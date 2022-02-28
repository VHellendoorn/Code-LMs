from typing import Tuple

import tensorflow as tf

from ..miner import get_session_pairs, get_session_triplets
from ..miner.base import BaseClassMiner, BaseSessionMiner
from ..miner.mining_strategies import TorchStrategicMiningHelper


class SiameseMiner(BaseClassMiner[tf.Tensor]):
    def mine(
        self, labels: tf.Tensor, distances: tf.Tensor
    ) -> Tuple[tf.Tensor, tf.Tensor, tf.Tensor]:
        """Generate all possible pairs.

        :param labels: A 1D tensor of item labels (classes)
        :param distances: A tensor matrix of pairwise distance between each two item
            embeddings

        :return: three 1D tensors, first one holding integers of first element of
            pair, second of the second element of pair, and third one the label (0 or
            1) for the pair for each pair
        """
        assert len(distances) == len(labels)

        l1, l2 = tf.expand_dims(labels, 1), tf.expand_dims(labels, 0)
        matches = tf.cast(l1 == l2, tf.uint8)
        diffs = 1 - matches
        matches = tf.experimental.numpy.triu(matches, 1)
        diffs = tf.experimental.numpy.triu(diffs)

        ind1_pos, ind2_pos = tf.unstack(tf.where(matches), axis=1)
        ind1_neg, ind2_neg = tf.unstack(tf.where(diffs), axis=1)

        ind1 = tf.concat([ind1_pos, ind1_neg], axis=0)
        ind2 = tf.concat([ind2_pos, ind2_neg], axis=0)

        target = tf.concat([tf.ones_like(ind1_pos), tf.zeros_like(ind1_neg)], axis=0)
        return ind1, ind2, target


class SiameseEasyHardMiner(BaseClassMiner[tf.Tensor]):
    def __init__(self, pos_strategy: str = 'hard', neg_strategy: str = 'hard'):
        """
        Miner implements easy-hard mining for tuples in siamese training.
        The following strategies are available.

        Pos. Strategy:
        - 'hard': Returns hardest positive (furthest) sample per anchor
        - 'semihard': Returns the hardest positive sample per anchor, such
          that it is closer than the selected negative
        - 'easy': Returns the easiest positive sample per anchor
        - 'all': Returns all positive samples

        Neg. Strategy:
        - 'hard': Returns hardest negative (closest) sample per anchor
        - 'semihard': Returns the hardest negative sample per anchor, such
          that it is further than the selected negative
        - 'easy': Returns the easiest negative sample per anchor
        - 'all': Returns all negative samples

        Not allowed:
        - pos. and neg. strategy cannot be set to 'semihard' simultaneously
        - When pos. or neg. strategy is set to 'semihard' the other cannot be
          set to 'all'

        :param pos_strategy: Strategy for selecting positive samples
        :param neg_strategy: Strategy for selecting negative samples
        """
        self.strategic_mining_helper = TorchStrategicMiningHelper(
            pos_strategy, neg_strategy
        )

    def mine(
        self, labels: tf.Tensor, distances: tf.Tensor
    ) -> Tuple[tf.Tensor, tf.Tensor, tf.Tensor]:
        """Generate all possible pairs.

        :param labels: A 1D tensor of item labels (classes)
        :param distances: A tensor matrix of pairwise distance between each two item
            embeddings

        :return: three 1D tensors, first one holding integers of first element of
            pair, second of the second element of pair, and third one the label (0 or
            1) for the pair for each pair
        """
        assert len(distances) == len(labels)

        l1, l2 = tf.expand_dims(labels, 1), tf.expand_dims(labels, 0)
        matches = tf.cast(l1 == l2, tf.uint8)
        diffs = 1 - matches
        matches = tf.experimental.numpy.triu(matches, 1)
        diffs = tf.experimental.numpy.triu(diffs)

        # Apply mining strategy
        updated_matches, updated_diffs = self.strategic_mining_helper.apply_strategy(
            matches.numpy(),
            diffs.numpy(),
            distances.numpy(),
            to_numpy=True,
        )
        matches = tf.convert_to_tensor(updated_matches, dtype=matches.dtype)
        diffs = tf.convert_to_tensor(updated_diffs, dtype=diffs.dtype)

        ind1_pos, ind2_pos = tf.unstack(tf.where(matches), axis=1)
        ind1_neg, ind2_neg = tf.unstack(tf.where(diffs), axis=1)

        ind1 = tf.concat([ind1_pos, ind1_neg], axis=0)
        ind2 = tf.concat([ind2_pos, ind2_neg], axis=0)

        target = tf.concat([tf.ones_like(ind1_pos), tf.zeros_like(ind1_neg)], axis=0)
        return ind1, ind2, target


class TripletMiner(BaseClassMiner[tf.Tensor]):
    def mine(
        self, labels: tf.Tensor, distances: tf.Tensor
    ) -> Tuple[tf.Tensor, tf.Tensor, tf.Tensor]:
        """Generate all possible triplets.

        :param labels: A 1D tensor of item labels (classes)
        :param distances: A tensor matrix of pairwise distance between each two item
            embeddings

        :return: three 1D tensors, holding the anchor index, positive index and
            negative index of each triplet, respectively
        """
        assert len(distances) == len(labels)

        l1, l2 = tf.expand_dims(labels, 1), tf.expand_dims(labels, 0)
        matches = tf.cast(l1 == l2, tf.uint8)
        diffs = matches ^ 1

        matches = tf.linalg.set_diag(matches, diagonal=tf.zeros_like(labels, tf.uint8))
        triplets = tf.expand_dims(matches, 2) * tf.expand_dims(diffs, 1)

        return tf.transpose(tf.where(triplets))


class TripletEasyHardMiner(BaseClassMiner[tf.Tensor]):
    def __init__(self, pos_strategy: str = 'hard', neg_strategy: str = 'hard'):
        """
        Miner implements easy-hard mining for triplets during training with
        triplet loss. The following strategies are available.

        Pos. Strategy:
        - 'hard': Returns hardest positive (furthest) sample per anchor
        - 'semihard': Returns the hardest positive sample per anchor, such
          that it is closer than the selected negative
        - 'easy': Returns the easiest positive sample per anchor
        - 'all': Returns all positive samples

        Neg. Strategy:
        - 'hard': Returns hardest negative (closest) sample per anchor
        - 'semihard': Returns the hardest negative sample per anchor, such
          that it is further than the selected negative
        - 'easy': Returns the easiest negative sample per anchor
        - 'all': Returns all negative samples

        Not allowed:
        - pos. and neg. strategy cannot be set to 'semihard' simultaneously
        - When pos. or neg. strategy is set to 'semihard' the other cannot be
          set to 'all'

        :param pos_strategy: Strategy for selecting positive samples
        :param neg_strategy: Strategy for selecting negative samples
        """
        self.strategic_mining_helper = TorchStrategicMiningHelper(
            pos_strategy, neg_strategy
        )

    def mine(
        self, labels: tf.Tensor, distances: tf.Tensor
    ) -> Tuple[tf.Tensor, tf.Tensor, tf.Tensor]:
        """Generate all possible triplets.

        :param labels: A 1D tensor of item labels (classes)
        :param distances: A tensor matrix of pairwise distance between each two item
            embeddings

        :return: three 1D tensors, holding the anchor index, positive index and
            negative index of each triplet, respectively
        """
        assert len(distances) == len(labels)

        l1, l2 = tf.expand_dims(labels, 1), tf.expand_dims(labels, 0)
        matches = tf.cast(l1 == l2, tf.uint8)
        diffs = matches ^ 1

        matches = tf.linalg.set_diag(matches, diagonal=tf.zeros_like(labels, tf.uint8))

        # Apply mining strategy
        updated_matches, updated_diffs = self.strategic_mining_helper.apply_strategy(
            matches.numpy(),
            diffs.numpy(),
            distances.numpy(),
            to_numpy=True,
        )

        matches = tf.convert_to_tensor(updated_matches, dtype=matches.dtype)
        diffs = tf.convert_to_tensor(updated_diffs, dtype=diffs.dtype)

        matches = tf.convert_to_tensor(updated_matches, dtype=matches.dtype)
        diffs = tf.convert_to_tensor(updated_diffs, dtype=diffs.dtype)

        triplets = tf.expand_dims(matches, 2) * tf.expand_dims(diffs, 1)

        return tf.transpose(tf.where(triplets))


class SiameseSessionMiner(BaseSessionMiner[tf.Tensor]):
    def mine(
        self, labels: Tuple[tf.Tensor, tf.Tensor], distances: tf.Tensor
    ) -> Tuple[tf.Tensor, tf.Tensor, tf.Tensor]:
        """Generate all possible pairs for each session.

        :param labels: A tuple of 1D tensors, denotind the items' session and match
            type (0 for anchor, 1 for postive match and -1 for negative match),
            respectively
        :param distances: A tensor matrix of pairwise distance between each two item
            embeddings

        :return: three numpy arrays, first one holding integers of first element of
            pair, second of the second element of pair, and third one the label (0 or
            1) for the pair for each pair
        """
        assert len(distances) == len(labels[0]) == len(labels[1])

        sessions, match_types = [x.numpy().tolist() for x in labels]
        ind_one, ind_two, labels_ret = get_session_pairs(sessions, match_types)

        return (
            tf.constant(ind_one),
            tf.constant(ind_two),
            tf.constant(labels_ret),
        )


class TripletSessionMiner(BaseSessionMiner[tf.Tensor]):
    def mine(
        self, labels: Tuple[tf.Tensor, tf.Tensor], distances: tf.Tensor
    ) -> Tuple[tf.Tensor, tf.Tensor, tf.Tensor]:
        """Generate all possible triplets for each session.

        :param labels: A tuple of 1D tensors, denotind the items' session and match
            type (0 for anchor, 1 for postive match and -1 for negative match),
            respectively
        :param distances: A tensor matrix of pairwise distance between each two item
            embeddings

        :return: three numpy arrays, holding the anchor index, positive index and
            negative index of each triplet, respectively
        """

        assert len(distances) == len(labels[0]) == len(labels[1])

        sessions, match_types = [x.numpy().tolist() for x in labels]
        anchor_ind, pos_ind, neg_ind = get_session_triplets(sessions, match_types)

        return (
            tf.constant(anchor_ind),
            tf.constant(pos_ind),
            tf.constant(neg_ind),
        )
