import paddle
import pytest
import tensorflow as tf
import torch

import finetuner
from finetuner.toydata import generate_fashion


@pytest.mark.parametrize('loss', ['SiameseLoss', 'TripletLoss'])
def test_fit_all(tmpdir, loss):
    embed_models = {
        'keras': lambda: tf.keras.Sequential(
            [
                tf.keras.layers.Flatten(input_shape=(28, 28)),
                tf.keras.layers.Dense(128, activation='relu'),
                tf.keras.layers.Dense(32),
            ]
        ),
        'pytorch': lambda: torch.nn.Sequential(
            torch.nn.Flatten(),
            torch.nn.Linear(
                in_features=28 * 28,
                out_features=128,
            ),
            torch.nn.ReLU(),
            torch.nn.Linear(in_features=128, out_features=32),
        ),
        'paddle': lambda: paddle.nn.Sequential(
            paddle.nn.Flatten(),
            paddle.nn.Linear(
                in_features=28 * 28,
                out_features=128,
            ),
            paddle.nn.ReLU(),
            paddle.nn.Linear(in_features=128, out_features=32),
        ),
    }

    for kb, b in embed_models.items():
        model = finetuner.fit(
            b(),
            loss=loss,
            train_data=generate_fashion(num_total=200),
            eval_data=generate_fashion(is_testset=True, num_total=100),
            batch_size=32,
            epochs=2,
        )
        assert model
