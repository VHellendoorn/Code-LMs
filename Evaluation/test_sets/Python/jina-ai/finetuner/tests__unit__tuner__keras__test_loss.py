import numpy as np
import pytest
import tensorflow as tf

from finetuner.tuner.keras.losses import (
    NTXentLoss,
    SiameseLoss,
    TripletLoss,
    get_distance,
)

N_BATCH = 10
N_DIM = 128

ALL_LOSSES = [SiameseLoss, TripletLoss]


@pytest.mark.parametrize('margin', [0.0, 0.5, 1.0])
@pytest.mark.parametrize('distance', ['cosine', 'euclidean'])
@pytest.mark.parametrize('loss_cls', ALL_LOSSES)
def test_loss_output(loss_cls, distance, margin):
    """Test that we get a single positive number as output"""
    loss = loss_cls(distance=distance, margin=margin)

    labels = np.ones((N_BATCH,))
    labels[: N_BATCH // 2] = 0
    labels = tf.convert_to_tensor(labels)
    embeddings = tf.random.uniform((N_BATCH, N_DIM))

    output = loss(embeddings, labels)

    # assert output.ndim == 0
    assert output >= 0


@pytest.mark.parametrize('distance', ['cosine', 'euclidean'])
@pytest.mark.parametrize('loss_cls', ALL_LOSSES)
def test_loss_zero_same(loss_cls, distance):
    """Sanity check that with perfectly separated embeddings, loss is zero"""

    # Might need to specialize this later
    loss = loss_cls(distance=distance, margin=0.0)

    labels = np.ones((N_BATCH,))
    labels[: N_BATCH // 2] = 0

    embeddings = np.ones((N_BATCH, N_DIM))
    embeddings[: N_BATCH // 2] *= -1

    labels = tf.convert_to_tensor(labels)
    embeddings = tf.convert_to_tensor(embeddings)

    output = loss(embeddings, labels)

    np.testing.assert_almost_equal(output.numpy(), 0, decimal=5)


@pytest.mark.parametrize(
    'loss_cls,indices,exp_result',
    [
        (SiameseLoss, [[0, 2], [1, 3], [0, 1]], 0.64142),
        (TripletLoss, [[0, 2], [1, 3], [2, 1]], 0.9293),
    ],
)
def test_compute(loss_cls, indices, exp_result):
    """Check that the compute function returns numerically correct results"""

    indices = [tf.constant(x) for x in indices]
    embeddings = tf.constant([[0.1, 0.1], [0.2, 0.2], [0.4, 0.4], [0.7, 0.7]])
    result = loss_cls(distance='euclidean').compute(embeddings, indices)
    np.testing.assert_almost_equal(result.numpy(), exp_result, decimal=5)


@pytest.mark.parametrize(
    'loss_cls',
    [SiameseLoss, TripletLoss],
)
def test_compute_loss_given_insufficient_data(loss_cls):
    indices = [tf.constant([]) for _ in range(3)]
    embeddings = tf.constant([[0.0, 0.1, 0.2, 0.4]])
    with pytest.raises(ValueError):
        loss_cls(distance='euclidean').compute(embeddings, indices)


@pytest.mark.gpu
@pytest.mark.parametrize(
    'loss_cls',
    [SiameseLoss, TripletLoss],
)
def test_compute_loss_given_insufficient_data_gpu(loss_cls):
    with tf.device('/GPU:0'):
        indices = [tf.constant([]) for _ in range(3)]
        embeddings = tf.constant([[0.0, 0.1, 0.2, 0.4]])
        with pytest.raises(ValueError):
            loss_cls(distance='euclidean').compute(embeddings, indices)


@pytest.mark.parametrize('labels', [[0, 1], [0, 0, 1], [0, 0, 0, 1, 1]])
def test_wrong_labels_ntxent_loss(labels):
    """Test cases where are not two views of each instance"""
    labels = tf.constant(labels)
    embeddings = tf.random.normal((len(labels), 2))
    loss_fn = NTXentLoss()

    with pytest.raises(ValueError, match="There need to be two views"):
        loss_fn(embeddings, labels)


@pytest.mark.parametrize('temp', [0.3, 0.5, 1.0])
@pytest.mark.parametrize('labels', [[0, 0, 1, 1], [0, 1, 0, 1], [0, 1, 2, 0, 1, 2]])
def test_correct_ntxent_loss(labels, temp):
    """Test that returned loss matches cross-entropy calculated semi-manually"""
    labels_tensor = tf.constant(labels)
    embeddings = tf.random.normal((len(labels), 2))
    loss_fn = NTXentLoss(temperature=temp)

    # Compute losses manually
    sim = (1 - get_distance(embeddings, 'cosine')) / temp
    losses = []
    for i in range(len(labels)):
        exclude_self = [j for j in range(len(labels)) if j != i]
        other_pos_ind = [labels[j] for j in exclude_self].index(labels[i])
        sim_ind = tf.stack([sim[i, ind] for ind in exclude_self])
        losses.append(-tf.nn.log_softmax(sim_ind, axis=0)[other_pos_ind].numpy())

    np.testing.assert_approx_equal(
        loss_fn(embeddings, labels_tensor).numpy(), np.mean(losses), 4
    )


@pytest.mark.parametrize('temp', [0.3, 0.5, 1.0])
@pytest.mark.parametrize('labels', [[0, 0, 1, 1], [0, 1, 0, 1], [0, 1, 2, 0, 1, 2]])
def test_correct_ntxent_loss_gpu(labels, temp):
    """Test that returned loss matches cross-entropy calculated semi-manually"""

    with tf.device('/GPU:0'):
        labels_tensor = tf.constant(labels)
        embeddings = tf.random.normal((len(labels), 2))
        loss_fn = NTXentLoss(temperature=temp)

        # Compute losses manually
        sim = (1 - get_distance(embeddings, 'cosine')) / temp
        losses = []
        for i in range(len(labels)):
            exclude_self = [j for j in range(len(labels)) if j != i]
            other_pos_ind = [labels[j] for j in exclude_self].index(labels[i])
            sim_ind = tf.stack([sim[i, ind] for ind in exclude_self])
            losses.append(-tf.nn.log_softmax(sim_ind, axis=0)[other_pos_ind].numpy())

        np.testing.assert_approx_equal(
            loss_fn(embeddings, labels_tensor).cpu().numpy(), np.mean(losses), 4
        )
