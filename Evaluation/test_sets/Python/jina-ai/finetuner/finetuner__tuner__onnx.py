from functools import partial
from typing import Any, List, Sequence, Tuple

import numpy as np

from finetuner.helper import AnyDNN, get_framework


def to_onnx(
    embed_model: AnyDNN,
    model_path: str,
    input_shape: Sequence[int],
    opset_version: int = 11,
    input_type: str = 'float32',
) -> None:
    """Func that takes a model in PaddlePaddle, PyTorch or Keras,
    and converts it to the ONNX format
    :params embed_model: Model to be converted and stored in ONNX
    :params model_path: Path to store ONNX model to
    :params input_shape: Input shape of embedding model
    :params opset_version: ONNX opset version in which to register
    :params input_type: Input type for model tracing during export of
      PyTorch-based models
    """
    if isinstance(input_shape, tuple):
        input_shape = list(input_shape)

    if not model_path.endswith('.onnx'):
        raise ValueError(
            f'The `model_path` needs to end with `.onnx`, but was: {model_path}'
        )

    fn = get_framework(embed_model)
    _to_onnx_func = None
    if fn == 'torch':
        _to_onnx_func = partial(_to_onnx_torch, input_type=input_type)
    elif fn == 'keras':
        _to_onnx_func = _to_onnx_keras
    else:
        _to_onnx_func = _to_onnx_paddle

    # Call onnx conversion
    _to_onnx_func(embed_model, model_path, input_shape, opset_version)

    _check_onnx_model(model_path)


def _check_onnx_model(model_path: str) -> None:
    """Check an ONNX model
    :params model_path: Path to ONNX model
    """
    import onnx

    model = onnx.load(model_path)
    onnx.checker.check_model(model)


def _to_onnx_torch(
    embed_model: AnyDNN,
    model_path: str,
    input_shape: Tuple[int, ...],
    opset_version: int = 11,
    batch_size: int = 16,
    input_type: str = 'float32',
) -> None:
    """Convert a PyTorch embedding model to the ONNX format
    :params embed_model: Embedding model to register in ONNX
    :params model_path: Patch where to register ONNX model to
    :params input_shape: Embedding model input shape
    :params batch_size: The batch size during export
    :params opset_version: ONNX opset version in which to register
    :params input_type: Input type for model tracing during export
      of PyTorch-based models
    """

    import torch

    model_device = next(embed_model.parameters()).device
    if model_device == "cuda":
        embed_model = embed_model.to(torch.device("cuda"))

    supported_types = {
        'float16': torch.float16,
        'float32': torch.float32,
        'float64': torch.float64,
    }
    if input_type not in supported_types:
        raise ValueError(
            f'The input_type should be one of: {[t for t in supported_types.keys()]} '
            f'but was: {input_type}'
        )

    x = torch.randn(
        [batch_size] + input_shape,
        requires_grad=True,
        dtype=supported_types[input_type],
    )
    # Set device to model device
    model_device = next(embed_model.parameters()).device
    x = x.to(model_device)

    torch.onnx.export(
        embed_model,
        x,
        model_path,
        do_constant_folding=True,
        opset_version=opset_version,
        input_names=['input'],
        output_names=['output'],
        dynamic_axes={'input': {0: 'batch_size'}, 'output': {0: 'batch_size'}},
    )


def _to_onnx_keras(
    embed_model: AnyDNN,
    model_path: str,
    input_shape: Tuple[int, ...],
    opset_version: int = 11,
) -> None:
    """Convert a Keras embedding model to the ONNX format
    :params embed_model: Embedding model to register in ONNX
    :params model_path: Patch where to register ONNX model to
    :params input_shape: Embedding model input shape
    :params opset_version: ONNX opset version in which to register
    """

    try:
        import tf2onnx
    except (ImportError, ModuleNotFoundError):
        raise ModuleNotFoundError('Module tf2onnx not found, try "pip install tf2onnx"')

    import tensorflow as tf

    shape = [
        None,
    ] + input_shape

    _ = tf2onnx.convert.from_keras(
        embed_model,
        input_signature=[tf.TensorSpec(shape)],
        opset=opset_version,
        output_path=model_path,
    )


def _to_onnx_paddle(
    embed_model: AnyDNN,
    model_path: str,
    input_shape: List[int],
    opset_version: int = 11,
    model_input_type: str = 'float32',
) -> None:
    """Convert a paddle embedding model to the ONNX format
    :params embed_model: Embedding model to register in ONNX
    :params model_path: Patch where to register ONNX model to
    :params input_shape: Embedding model input shape
    :params opset_version: ONNX opset version in which to register
    :params model_input_type: Data type model expects
    """

    # Removing onnx extension as paddle adds it automatically
    if model_path.endswith('.onnx'):
        model_path = model_path[:-5]

    import paddle
    from paddle.static import InputSpec

    shape = [None] + list(input_shape)
    x_spec = InputSpec(shape, model_input_type, 'input')

    paddle.onnx.export(
        embed_model,
        model_path,
        input_spec=[x_spec],
        opset_version=opset_version,
    )


def validate_onnx_export(
    embed_model: AnyDNN,
    export_path: str,
    input_shape: Tuple[int, ...],
) -> None:
    """
    Test an exported model by comparing the outputs of the original and the exported model
    against the same input.
    :params embed_model: The original embedding model. Can be either a PyTorch module,
        a Keras model or a PaddlePaddle layer.
    :params export_path: The path where the exported model is stored.
    :params input_shape: The model's expected input shape, without the batch axis.
    """
    import onnxruntime

    fm = get_framework(embed_model)

    def _from_numpy(array: np.ndarray) -> Any:
        if fm == 'torch':
            import torch

            return torch.tensor(array)
        elif fm == 'keras':
            import tensorflow as tf

            return tf.convert_to_tensor(array)
        else:
            import paddle

            return paddle.Tensor(array)

    def _to_numpy(tensor: Any) -> np.ndarray:
        if fm == 'torch':
            return (
                tensor.detach().cpu().numpy()
                if tensor.requires_grad
                else tensor.cpu().numpy()
            )
        else:
            return tensor.numpy()

    BATCH_SIZE = 8
    shape = [BATCH_SIZE] + input_shape
    x = np.random.rand(*shape).astype(np.float32)

    # Create onnx session and and run onnx model inference
    session = onnxruntime.InferenceSession(export_path)
    y_exported = session.run(None, {session.get_inputs()[0].name: x})[0]

    # Create framework-specific tensor
    x = _from_numpy(x)

    # Send test data to same device as model
    if fm == 'torch':
        model_device = next(embed_model.parameters()).device
        x = x.to(model_device)

    is_training_before = False
    if fm == 'torch':
        is_training_before = embed_model.training
        embed_model.eval()

    y_original = _to_numpy(embed_model(x))

    if is_training_before:
        embed_model.train()

    np.testing.assert_allclose(y_original, y_exported, rtol=1e-03, atol=1e-05)
