from __future__ import print_function, absolute_import, division

import pytest
import warnings

from astropy.io import fits
import numpy as np

from ..wcs_utils import (WCS, drop_axis, wcs_swapaxes, add_stokes_axis_to_wcs,
                         axis_names, slice_wcs, check_equality, strip_wcs_from_header)
from . import path


def test_wcs_dropping():
    wcs = WCS(naxis=4)
    wcs.wcs.pc = np.zeros([4, 4])
    np.fill_diagonal(wcs.wcs.pc, np.arange(1, 5))
    pc = wcs.wcs.pc  # for later use below

    dropped = drop_axis(wcs, 0)
    assert np.all(dropped.wcs.get_pc().diagonal() == np.array([2, 3, 4]))
    dropped = drop_axis(wcs, 1)
    assert np.all(dropped.wcs.get_pc().diagonal() == np.array([1, 3, 4]))
    dropped = drop_axis(wcs, 2)
    assert np.all(dropped.wcs.get_pc().diagonal() == np.array([1, 2, 4]))
    dropped = drop_axis(wcs, 3)
    assert np.all(dropped.wcs.get_pc().diagonal() == np.array([1, 2, 3]))

    wcs = WCS(naxis=4)
    wcs.wcs.cd = pc

    dropped = drop_axis(wcs, 0)
    assert np.all(dropped.wcs.get_pc().diagonal() == np.array([2, 3, 4]))
    dropped = drop_axis(wcs, 1)
    assert np.all(dropped.wcs.get_pc().diagonal() == np.array([1, 3, 4]))
    dropped = drop_axis(wcs, 2)
    assert np.all(dropped.wcs.get_pc().diagonal() == np.array([1, 2, 4]))
    dropped = drop_axis(wcs, 3)
    assert np.all(dropped.wcs.get_pc().diagonal() == np.array([1, 2, 3]))


def test_wcs_swapping():
    wcs = WCS(naxis=4)
    wcs.wcs.pc = np.zeros([4, 4])
    np.fill_diagonal(wcs.wcs.pc, np.arange(1, 5))
    pc = wcs.wcs.pc  # for later use below

    swapped = wcs_swapaxes(wcs, 0, 1)
    assert np.all(swapped.wcs.get_pc().diagonal() == np.array([2, 1, 3, 4]))
    swapped = wcs_swapaxes(wcs, 0, 3)
    assert np.all(swapped.wcs.get_pc().diagonal() == np.array([4, 2, 3, 1]))
    swapped = wcs_swapaxes(wcs, 2, 3)
    assert np.all(swapped.wcs.get_pc().diagonal() == np.array([1, 2, 4, 3]))

    wcs = WCS(naxis=4)
    wcs.wcs.cd = pc

    swapped = wcs_swapaxes(wcs, 0, 1)
    assert np.all(swapped.wcs.get_pc().diagonal() == np.array([2, 1, 3, 4]))
    swapped = wcs_swapaxes(wcs, 0, 3)
    assert np.all(swapped.wcs.get_pc().diagonal() == np.array([4, 2, 3, 1]))
    swapped = wcs_swapaxes(wcs, 2, 3)
    assert np.all(swapped.wcs.get_pc().diagonal() == np.array([1, 2, 4, 3]))


def test_add_stokes():
    wcs = WCS(naxis=3)

    for ii in range(4):
        outwcs = add_stokes_axis_to_wcs(wcs, ii)
        assert outwcs.wcs.naxis == 4


def test_axis_names(data_adv, data_vad):
    wcs = WCS(str(data_adv))
    assert axis_names(wcs) == ['RA', 'DEC', 'VOPT']
    wcs = WCS(str(data_vad))
    assert axis_names(wcs) == ['VOPT', 'RA', 'DEC']


def test_wcs_slice():
    wcs = WCS(naxis=3)
    wcs.wcs.crpix = [50., 45., 30.]
    wcs_new = slice_wcs(wcs, (slice(10,20), slice(None), slice(20,30)))
    np.testing.assert_allclose(wcs_new.wcs.crpix, [30., 45., 20.])

def test_wcs_slice_reversal():
    wcs = WCS(naxis=3)
    wcs.wcs.crpix = [50., 45., 30.]
    wcs.wcs.crval = [0., 0., 0.]
    wcs.wcs.cdelt = [1., 1., 1.]
    wcs_new = slice_wcs(wcs, (slice(None, None, -1), slice(None), slice(None)),
                        shape=[100., 150., 200.])
    spaxis = wcs.sub([0]).wcs_pix2world(np.arange(100), 0)
    new_spaxis = wcs_new.sub([0]).wcs_pix2world(np.arange(100), 0)

    np.testing.assert_allclose(spaxis, new_spaxis[::-1])

def test_reversal_roundtrip():
    wcs = WCS(naxis=3)
    wcs.wcs.crpix = [50., 45., 30.]
    wcs.wcs.crval = [0., 0., 0.]
    wcs.wcs.cdelt = [1., 1., 1.]
    wcs_new = slice_wcs(wcs, (slice(None, None, -1), slice(None), slice(None)),
                        shape=[100., 150., 200.])
    spaxis = wcs.sub([0]).wcs_pix2world(np.arange(100), 0)

    new_spaxis = wcs_new.sub([0]).wcs_pix2world(np.arange(100), 0)

    np.testing.assert_allclose(spaxis, new_spaxis[::-1])

    re_reverse = slice_wcs(wcs_new, (slice(None, None, -1), slice(None), slice(None)),
                           shape=[100., 150., 200.])
    new_spaxis = re_reverse.sub([0]).wcs_pix2world(np.arange(100), 0)

    np.testing.assert_allclose(spaxis, new_spaxis[::-1])

    #These are NOT equal, but they are equivalent: CRVAL and CRPIX are shifted
    #by an acceptable amount
    # assert check_equality(wcs, re_reverse)

    re_re_reverse = slice_wcs(re_reverse, (slice(None, None, -1), slice(None),
                                           slice(None)),
                              shape=[100., 150., 200.])
    re_re_re_reverse = slice_wcs(re_re_reverse, (slice(None, None, -1),
                                                 slice(None), slice(None)),
                                 shape=[100., 150., 200.])

    assert check_equality(re_re_re_reverse, re_reverse)

def test_wcs_comparison():
    wcs1 = WCS(naxis=3)
    wcs1.wcs.crpix = np.array([50., 45., 30.], dtype='float32')

    wcs2 = WCS(naxis=3)
    wcs2.wcs.crpix = np.array([50., 45., 30.], dtype='float64')

    wcs3 = WCS(naxis=3)
    wcs3.wcs.crpix = np.array([50., 45., 31.], dtype='float64')

    wcs4 = WCS(naxis=3)
    wcs4.wcs.crpix = np.array([50., 45., 30.0001], dtype='float64')

    assert check_equality(wcs1,wcs2)
    assert not check_equality(wcs1,wcs3)
    assert check_equality(wcs1, wcs3, wcs_tolerance=1.0e1)
    assert not check_equality(wcs1,wcs4)
    assert check_equality(wcs1, wcs4, wcs_tolerance=1e-3)

@pytest.mark.parametrize('fn', ('cubewcs1.hdr', 'cubewcs2.hdr'))
def test_strip_wcs(fn):

    header1 = fits.Header.fromtextfile(path(fn))
    header1_stripped = strip_wcs_from_header(header1)

    with open(path(fn),'r') as fh:
        hdrlines = fh.readlines()

    newfn = fn.replace('.hdr', '_blanks.hdr')

    hdrlines.insert(-20,"\n")
    hdrlines.insert(-1,"\n")
    with open(path(newfn),'w') as fh:
        fh.writelines(hdrlines)

    header2 = fits.Header.fromtextfile(path(newfn))
    header2_stripped = strip_wcs_from_header(header2)

    assert header1_stripped == header2_stripped

def test_wcs_slice_unmatched_celestial():
    wcs = WCS(naxis=3)
    wcs.wcs.ctype = ['RA---TAN', 'DEC--TAN', 'FREQ']
    wcs.wcs.crpix = [50., 45., 30.]

    # drop RA
    with warnings.catch_warnings(record=True) as wrn:
        wcs_new = drop_axis(wcs, 0)

    assert 'is being removed' in str(wrn[-1].message)

    # drop Dec
    with warnings.catch_warnings(record=True) as wrn:
        wcs_new = drop_axis(wcs, 1)

    assert 'is being removed' in str(wrn[-1].message)

    with warnings.catch_warnings(record=True) as wrn:
        wcs_new = slice_wcs(wcs, (slice(10,20), 0, slice(20,30)),
                            drop_degenerate=True)

    assert 'is being removed' in str(wrn[-1].message)

def test_wcs_downsampling():
    """
    Regression tests for #525

    These are a series of simple tests I verified with pen and paper, but it's
    always worth checking me again.
    """
    wcs = WCS(naxis=1)
    wcs.wcs.ctype = ['FREQ',]
    wcs.wcs.crpix = [1.,]

    nwcs = slice_wcs(wcs, slice(0, None, 1))
    assert nwcs.wcs.crpix[0] == 1

    nwcs = slice_wcs(wcs, slice(0, None, 2))
    assert nwcs.wcs.crpix[0] == 0.75

    nwcs = slice_wcs(wcs, slice(0, None, 4))
    assert nwcs.wcs.crpix[0] == 0.625

    nwcs = slice_wcs(wcs, slice(2, None, 1))
    assert nwcs.wcs.crpix[0] == -1

    nwcs = slice_wcs(wcs, slice(2, None, 2))
    assert nwcs.wcs.crpix[0] == -0.25

    nwcs = slice_wcs(wcs, slice(2, None, 4))
    assert nwcs.wcs.crpix[0] == 0.125
