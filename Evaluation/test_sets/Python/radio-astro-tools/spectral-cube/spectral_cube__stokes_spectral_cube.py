from __future__ import print_function, absolute_import, division

import six
import numpy as np

from astropy.io.registry import UnifiedReadWriteMethod
from .io.core import StokesSpectralCubeRead, StokesSpectralCubeWrite
from .spectral_cube import SpectralCube, BaseSpectralCube
from . import wcs_utils
from .masks import BooleanArrayMask, is_broadcastable_and_smaller

__all__ = ['StokesSpectralCube']

VALID_STOKES = ['I', 'Q', 'U', 'V', 'RR', 'LL', 'RL', 'LR', 'XX', 'XY', 'YX', 'YY', 
                'RX', 'RY', 'LX', 'LY', 'XR,', 'XL', 'YR', 'YL', 'PP', 'PQ', 'QP', 'QQ', 
                'RCircular', 'LCircular', 'Linear', 'Ptotal', 'Plinear', 'PFtotal', 
                'PFlinear', 'Pangle']


class StokesSpectralCube(object):
    """
    A class to store a spectral cube with multiple Stokes parameters.

    The individual Stokes cubes can share a common mask in addition to having
    component-specific masks.
    """

    def __init__(self, stokes_data, mask=None, meta=None, fill_value=None):

        self._stokes_data = stokes_data
        self._meta = meta or {}
        self._fill_value = fill_value

        reference = tuple(stokes_data.keys())[0]

        for component in stokes_data:

            if not isinstance(stokes_data[component], BaseSpectralCube):
                raise TypeError("stokes_data should be a dictionary of "
                                "SpectralCube objects")

            if not wcs_utils.check_equality(stokes_data[component].wcs,
                                            stokes_data[reference].wcs):
                raise ValueError("All spectral cubes in stokes_data "
                                 "should have the same WCS")

            if component not in VALID_STOKES:
                raise ValueError("Invalid Stokes component: {0} - should be "
                                 "one of I, Q, U, V, RR, LL, RL, LR, XX, XY, YX, YY, RX, RY, LX, LY, XR, XL, YR, YL, PP, PQ, QP, QQ, RCircular, LCircular, Linear, Ptotal, Plinear, PFtotal, PFlinear, Pangle".format(component))

            if stokes_data[component].shape != stokes_data[reference].shape:
                raise ValueError("All spectral cubes should have the same shape")

        self._wcs = stokes_data[reference].wcs
        self._shape = stokes_data[reference].shape

        if isinstance(mask, BooleanArrayMask):
            if not is_broadcastable_and_smaller(mask.shape, self._shape):
                raise ValueError("Mask shape is not broadcastable to data shape:"
                                 " {0} vs {1}".format(mask.shape, self._shape))

        self._mask = mask

    def __getitem__(self, key):
        if key in self._stokes_data:
            return self._stokes_data[key]
        else:
            raise KeyError("Key {0} does not exist in this cube.".format(key))

    def __setitem__(self, key, item):
        if key in self._stokes_data:
            self._stokes_data[key] = item
        else:
            errmsg = "Assigning new Stokes axes is not yet supported."
            raise NotImplementedError(errmsg)

    @property
    def shape(self):
        return self._shape

    @property
    def stokes_data(self):
        """
        The underlying data
        """
        return self._stokes_data

    @property
    def mask(self):
        """
        The underlying mask
        """
        return self._mask

    @property
    def wcs(self):
        return self._wcs

    def __dir__(self):
        if six.PY2:
            return self.components + dir(type(self)) + list(self.__dict__)
        else:
            return self.components + super(StokesSpectralCube, self).__dir__()

    @property
    def components(self):
        return list(self._stokes_data.keys())

    def __getattr__(self, attribute):
        """
        Descriptor to return the Stokes cubes
        """
        if attribute in self._stokes_data:
            if self.mask is not None:
                return self._stokes_data[attribute].with_mask(self.mask)
            else:
                return self._stokes_data[attribute]
        else:
            raise AttributeError("StokesSpectralCube has no attribute {0}".format(attribute))

    def with_mask(self, mask, inherit_mask=True):
        """
        Return a new StokesSpectralCube instance that contains a composite mask
        of the current StokesSpectralCube and the new ``mask``.

        Parameters
        ----------
        mask : :class:`MaskBase` instance, or boolean numpy array
            The mask to apply. If a boolean array is supplied,
            it will be converted into a mask, assuming that
            `True` values indicate included elements.

        inherit_mask : bool (optional, default=True)
            If True, combines the provided mask with the
            mask currently attached to the cube

        Returns
        -------
        new_cube : :class:`StokesSpectralCube`
            A cube with the new mask applied.

        Notes
        -----
        This operation returns a view into the data, and not a copy.
        """
        if isinstance(mask, np.ndarray):
            if not is_broadcastable_and_smaller(mask.shape, self.shape):
                raise ValueError("Mask shape is not broadcastable to data shape: "
                                 "%s vs %s" % (mask.shape, self.shape))
            mask = BooleanArrayMask(mask, self.wcs)

        if self._mask is not None:
            return self._new_cube_with(mask=self.mask & mask if inherit_mask else mask)
        else:
            return self._new_cube_with(mask=mask)

    def _new_cube_with(self, stokes_data=None,
                       mask=None, meta=None, fill_value=None):

        data = self._stokes_data if stokes_data is None else stokes_data
        mask = self._mask if mask is None else mask
        if meta is None:
            meta = {}
            meta.update(self._meta)

        fill_value = self._fill_value if fill_value is None else fill_value

        cube = StokesSpectralCube(stokes_data=data, mask=mask,
                                  meta=meta, fill_value=fill_value)

        return cube

    def with_spectral_unit(self, unit, **kwargs):

        stokes_data = {k: self._stokes_data[k].with_spectral_unit(unit, **kwargs)
                       for k in self._stokes_data}

        return self._new_cube_with(stokes_data=stokes_data)

    read = UnifiedReadWriteMethod(StokesSpectralCubeRead)
    write = UnifiedReadWriteMethod(StokesSpectralCubeWrite)
