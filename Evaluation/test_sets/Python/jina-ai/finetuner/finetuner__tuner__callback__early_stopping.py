import logging
from typing import TYPE_CHECKING, Optional

import numpy as np

from .base import BaseCallback

if TYPE_CHECKING:
    from ..base import BaseTuner


class EarlyStopping(BaseCallback):
    """
    Callback to stop training when a monitored metric has stopped improving.
    A `finetuner.fit()` training loop will check at the end of every epoch whether
    the monitored metric is still improving or not.
    """

    def __init__(
        self,
        monitor: str = 'val_loss',
        mode: str = 'auto',
        patience: int = 2,
        min_delta: int = 0,
        baseline: Optional[float] = None,
        verbose: bool = False,
    ):
        """
        :param monitor: if `monitor='train_loss'` training will stop/resume according
            to the training loss, while if `monitor='val_loss'` training will stop/resume
            according to the validation loss. If monitor is set to an evaluation metric,
            training will stop/resume according to the value of this metric.
        :param mode: one of {'auto', 'min', 'max'}. The decision to overwrite the
            current best monitor value is made based on either the maximization or the
            minimization of the monitored quantity.
            For an evaluation metric, this should be `max`, for `val_loss` this should
            be `min`, etc. In `auto` mode, the mode is set to `min` if `monitor='loss'`
            or `monitor='val_loss'` and to `min` otherwise.
        :param patience: integer, the number of epochs after which the training is
            stopped if there is no improvement. For example for `patience = 2`', if the model
            doesn't improve for 2 consecutive epochs, the training is stopped.
        :param min_delta: Minimum change in the monitored quantity to qualify as an
            improvement, i.e. an absolute change of less than min_delta, will count as no
            improvement.
        :param baseline: Baseline value for the monitored quantity.
            Training will stop if the model doesn't show improvement over the
            baseline.
        :param verbose: Wheter to log score improvement events.
        """
        self._logger = logging.getLogger('finetuner.' + self.__class__.__name__)
        self._logger.setLevel(logging.INFO if verbose else logging.WARNING)

        self._monitor = monitor
        self._patience = patience
        self._min_delta = min_delta
        self._baseline = baseline
        self._train_losses = []
        self._val_losses = []
        self._epoch_counter = 0

        if mode not in ['auto', 'min', 'max']:
            self._logger.warning(
                f'Unknown early stopping mode {mode}, falling back to auto mode.'
            )
            mode = 'auto'
        self._mode = mode

        self._monitor_op: np.ufunc
        self._best: float

        if mode == 'min':
            self._set_min_mode()
        elif mode == 'max':
            self._set_max_mode()
        else:
            if self._monitor == 'train_loss' or self._monitor == 'val_loss':
                self._set_min_mode()
            else:
                self._set_max_mode()

    def _set_max_mode(self):
        self._monitor_op = np.greater
        self._best = -np.Inf
        self._min_delta *= 1

    def _set_min_mode(self):
        self._monitor_op = np.less
        self._best = np.Inf
        self._min_delta *= -1

    def _check(self, tuner: 'BaseTuner'):
        """
        Checks if training should be stopped.
        """
        if self._baseline is not None:
            self._best = self._baseline

        if self._monitor == 'train_loss':
            current = np.mean(self._train_losses)
        elif self._monitor == 'val_loss':
            current = np.mean(self._val_losses)
        else:
            current = tuner.state.eval_metrics.get(self._monitor, None)

        if current is None:
            self._logger.warning(f'Could not retrieve monitor metric {self._monitor}')
            return

        if self._monitor_op(current - self._min_delta, self._best):
            self._logger.info(f'Model improved from {self._best} to {current}')
            self._best = current
            self._epoch_counter = 0
        else:
            self._epoch_counter += 1
            if self._epoch_counter == self._patience:
                self._logger.info(
                    f'Training is stopping, no improvement for {self._patience} epochs'
                )
                tuner.stop_training = True

    def on_train_batch_end(self, tuner: 'BaseTuner'):
        self._train_losses.append(tuner.state.current_loss)

    def on_val_batch_end(self, tuner: 'BaseTuner'):
        self._val_losses.append(tuner.state.current_loss)

    def on_epoch_end(self, tuner: 'BaseTuner'):
        self._check(tuner)
        self._train_losses = []
        self._val_losses = []
