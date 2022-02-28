use std::any::Any;
use std::collections::HashMap;

use ast::Float;
use ast::Int;
use errors::ElmError;
use errors::InterpreterError;
use errors::Wrappable;
use types::Value;

// TODO convert to postfix function calls
pub fn float_of(value: &Value) -> Result<f32, ElmError> {
    match value {
        Value::Number(a) => Ok(*a as f32),
        Value::Float(a) => Ok(*a),
        _ => {
            Err(InterpreterError::ExpectedFloat(value.clone()).wrap())
        }
    }
}

pub fn int_of(value: &Value) -> Result<i32, ElmError> {
    match value {
        Value::Number(a) => Ok(*a),
        Value::Int(a) => Ok(*a),
        _ => {
            Err(InterpreterError::ExpectedInt(value.clone()).wrap())
        }
    }
}

pub fn char_of(value: &Value) -> Result<char, ElmError> {
    match value {
        Value::Char(a) => Ok(*a),
        _ => {
            Err(InterpreterError::ExpectedChar(value.clone()).wrap())
        }
    }
}

pub fn string_of(value: &Value) -> Result<String, ElmError> {
    match value {
        Value::String(string) => Ok(string.clone()),
        _ => {
            Err(InterpreterError::ExpectedString(value.clone()).wrap())
        }
    }
}

pub fn str_of(value: &Value) -> Result<&str, ElmError> {
    match value {
        Value::String(string) => Ok(string.as_ref()),
        _ => {
            Err(InterpreterError::ExpectedString(value.clone()).wrap())
        }
    }
}

pub fn list_of(value: &Value) -> Result<&[Value], ElmError> {
    match value {
        Value::List(vec) => Ok(vec.as_slice()),
        _ => {
            Err(InterpreterError::ExpectedList(value.clone()).wrap())
        }
    }
}

pub fn bool_of(value: &Value) -> Result<bool, ElmError> {
    match value {
        Value::Adt(name, _, _) => Ok(name == "True"),
        _ => {
            Err(InterpreterError::ExpectedBoolean(value.clone()).wrap())
        }
    }
}

pub fn convert_to_rust(value: &Value) -> Option<Box<Any>> {
    match value {
        Value::Unit => {
            return Some(Box::new(()));
        }
        Value::Number(val) => {
            return Some(Box::new(val.clone()));
        }
        Value::Int(val) => {
            return Some(Box::new(val.clone()));
        }
        Value::Float(val) => {
            return Some(Box::new(val.clone()));
        }
        Value::String(val) => {
            return Some(Box::new(val.clone()));
        }
        Value::Char(val) => {
            return Some(Box::new(val.clone()));
        }
        Value::List(items) => {
            return Some(Box::new(items.clone()));
        }
        Value::Tuple(items) => {
            return Some(Box::new(items.clone()));
        }
        Value::Record(entries) => {
            return Some(Box::new(entries.clone()));
        }
        Value::Adt(_, _, _) => {
            return None;
        }
        Value::Fun { .. } => {
            return None;
        }
    }
}

pub fn convert_from_rust(val: &Any) -> Option<Value> {
    if let Some(()) = val.downcast_ref::<()>() {
        return Some(Value::Unit);
    }
    if let Some(unwrapped) = val.downcast_ref::<Int>() {
        return Some(Value::Int(*unwrapped));
    }
    if let Some(unwrapped) = val.downcast_ref::<Float>() {
        return Some(Value::Float(*unwrapped));
    }
    if let Some(unwrapped) = val.downcast_ref::<String>() {
        return Some(Value::String(unwrapped.clone()));
    }
    if let Some(unwrapped) = val.downcast_ref::<char>() {
        return Some(Value::Char(*unwrapped));
    }

    if let Some(unwrapped) = val.downcast_ref::<Vec<Box<Any>>>() {
        let values = unwrapped.iter()
            .map(|t| convert_from_rust(t))
            .collect::<Option<Vec<Value>>>()?;

        return Some(Value::List(values));
    }

    if let Some(unwrapped) = val.downcast_ref::<HashMap<String, Box<Any>>>() {
        let mut values: Vec<(String, Value)> = vec![];

        for (key, value) in unwrapped {
            values.push((key.clone(), convert_from_rust(value)?));
        }

        return Some(Value::Record(values));
    }

    if let Some(unwrapped) = val.downcast_ref::<(Box<Any>, Box<Any>)>() {
        let values = vec![
            convert_from_rust(&*unwrapped.0)?,
            convert_from_rust(&*unwrapped.1)?,
        ];
        return Some(Value::Tuple(values));
    }
    if let Some(unwrapped) = val.downcast_ref::<(Box<Any>, Box<Any>, Box<Any>)>() {
        let values = vec![
            convert_from_rust(&*unwrapped.0)?,
            convert_from_rust(&*unwrapped.1)?,
            convert_from_rust(&*unwrapped.2)?,
        ];
        return Some(Value::Tuple(values));
    }
    if let Some(unwrapped) = val.downcast_ref::<(Box<Any>, Box<Any>, Box<Any>, Box<Any>)>() {
        let values = vec![
            convert_from_rust(&*unwrapped.0)?,
            convert_from_rust(&*unwrapped.1)?,
            convert_from_rust(&*unwrapped.2)?,
            convert_from_rust(&*unwrapped.3)?,
        ];
        return Some(Value::Tuple(values));
    }
    if let Some(unwrapped) = val.downcast_ref::<(Box<Any>, Box<Any>, Box<Any>, Box<Any>, Box<Any>)>() {
        let values = vec![
            convert_from_rust(&*unwrapped.0)?,
            convert_from_rust(&*unwrapped.1)?,
            convert_from_rust(&*unwrapped.2)?,
            convert_from_rust(&*unwrapped.3)?,
            convert_from_rust(&*unwrapped.4)?,
        ];
        return Some(Value::Tuple(values));
    }

    if let Some(unwrapped) = val.downcast_ref::<(Box<Any>, Box<Any>, Box<Any>, Box<Any>, Box<Any>, Box<Any>)>() {
        let values = vec![
            convert_from_rust(&*unwrapped.0)?,
            convert_from_rust(&*unwrapped.1)?,
            convert_from_rust(&*unwrapped.2)?,
            convert_from_rust(&*unwrapped.3)?,
            convert_from_rust(&*unwrapped.4)?,
            convert_from_rust(&*unwrapped.5)?,
        ];
        return Some(Value::Tuple(values));
    }

    if let Some(unwrapped) = val.downcast_ref::<(Box<Any>, Box<Any>, Box<Any>, Box<Any>, Box<Any>, Box<Any>, Box<Any>)>() {
        let values = vec![
            convert_from_rust(&*unwrapped.0)?,
            convert_from_rust(&*unwrapped.1)?,
            convert_from_rust(&*unwrapped.2)?,
            convert_from_rust(&*unwrapped.3)?,
            convert_from_rust(&*unwrapped.4)?,
            convert_from_rust(&*unwrapped.5)?,
            convert_from_rust(&*unwrapped.6)?,
        ];
        return Some(Value::Tuple(values));
    }

    if let Some(unwrapped) = val.downcast_ref::<(Box<Any>, Box<Any>, Box<Any>, Box<Any>, Box<Any>, Box<Any>, Box<Any>, Box<Any>)>() {
        let values = vec![
            convert_from_rust(&*unwrapped.0)?,
            convert_from_rust(&*unwrapped.1)?,
            convert_from_rust(&*unwrapped.2)?,
            convert_from_rust(&*unwrapped.3)?,
            convert_from_rust(&*unwrapped.4)?,
            convert_from_rust(&*unwrapped.5)?,
            convert_from_rust(&*unwrapped.6)?,
            convert_from_rust(&*unwrapped.7)?,
        ];
        return Some(Value::Tuple(values));
    }

    None
}

#[derive(Debug, Eq, PartialEq)]
enum NumberState {
    Number,
    Int,
    Float,
}

pub fn number_op<F: FnOnce(f32, f32) -> f32>(val_a: &Value, val_b: &Value, op: F) -> Result<Value, ElmError> {
    let mut strong_type: NumberState;

    let a = match val_a {
        Value::Number(a) => {
            strong_type = NumberState::Number;
            *a as f32
        }
        Value::Int(a) => {
            strong_type = NumberState::Int;
            *a as f32
        }
        Value::Float(a) => {
            strong_type = NumberState::Float;
            *a
        }
        _ => {
            return Err(InterpreterError::ExpectedNumber(val_a.clone()).wrap());
        }
    };

    let b = match val_b {
        Value::Number(a) => {
            strong_type = merge(strong_type, NumberState::Number, val_b)?;
            *a as f32
        }
        Value::Int(a) => {
            strong_type = merge(strong_type, NumberState::Int, val_b)?;
            *a as f32
        }
        Value::Float(a) => {
            strong_type = merge(strong_type, NumberState::Float, val_b)?;
            *a
        }
        _ => {
            return Err(InterpreterError::ExpectedNumber(val_a.clone()).wrap());
        }
    };

    let result = op(a, b);

    Ok(match strong_type {
        NumberState::Number => Value::Number(result as i32),
        NumberState::Int => Value::Int(result as i32),
        NumberState::Float => Value::Float(result),
    })
}

/*
Truth table of number for:
(+) : number, number -> number

Float, Float -> Float
number, Float -> Float
Float, number -> Float

Int, Int -> Int
number, Int -> Int
Int, number -> Int

Int, Float -> error
Float, Int -> error
*/
fn merge(a: NumberState, b: NumberState, value: &Value) -> Result<NumberState, ElmError> {
    match a {
        NumberState::Number => Ok(b),
        NumberState::Int => {
            if b == NumberState::Int || b == NumberState::Number {
                Ok(a)
            } else {
                Err(InterpreterError::ExpectedInt(value.clone()).wrap())
            }
        }
        NumberState::Float => {
            if b == NumberState::Float || b == NumberState::Number {
                Ok(a)
            } else {
                Err(InterpreterError::ExpectedFloat(value.clone()).wrap())
            }
        }
    }
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn check_int() {
        let result = convert_to_rust(&convert_from_rust(&1).unwrap()).unwrap();
        assert_eq!(*result.downcast::<Int>().unwrap(), 1);
    }

    #[test]
    fn check_float() {
        let result = convert_to_rust(&convert_from_rust(&(1.5 as Float)).unwrap()).unwrap();
        assert_eq!(*result.downcast::<Float>().unwrap(), 1.5);
    }

    #[test]
    fn check_string() {
        let result = convert_to_rust(&convert_from_rust(&String::from("Hello world")).unwrap()).unwrap();
        assert_eq!(&*result.downcast::<String>().unwrap(), "Hello world");
    }
}