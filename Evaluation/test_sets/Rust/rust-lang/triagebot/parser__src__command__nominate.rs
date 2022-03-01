//! The beta nomination command parser.
//!
//! The grammar is as follows:
//!
//! ```text
//! Command:
//! `@bot beta-nominate <team>`.
//! `@bot nominate <team>`.
//! `@bot beta-accept`.
//! `@bot beta-approve`.
//! ```
//!
//! This constrains to just one team; users should issue the command multiple
//! times if they want to nominate for more than one team. This is to encourage
//! descriptions of what to do targeted at each team, rather than a general
//! summary.

use crate::error::Error;
use crate::token::{Token, Tokenizer};
use std::fmt;

#[derive(PartialEq, Eq, Debug)]
pub struct NominateCommand {
    pub team: String,
    pub style: Style,
}

#[derive(Debug, PartialEq, Eq, Copy, Clone)]
pub enum Style {
    Beta,
    BetaApprove,
    Decision,
}

#[derive(PartialEq, Eq, Debug)]
pub enum ParseError {
    ExpectedEnd,
    NoTeam,
}

impl std::error::Error for ParseError {}

impl fmt::Display for ParseError {
    fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
        match self {
            ParseError::ExpectedEnd => write!(f, "expected end of command"),
            ParseError::NoTeam => write!(f, "no team specified"),
        }
    }
}

impl NominateCommand {
    pub fn parse<'a>(input: &mut Tokenizer<'a>) -> Result<Option<Self>, Error<'a>> {
        let mut toks = input.clone();
        let style = match toks.peek_token()? {
            Some(Token::Word("beta-nominate")) => Style::Beta,
            Some(Token::Word("nominate")) => Style::Decision,
            Some(Token::Word("beta-accept")) => Style::BetaApprove,
            Some(Token::Word("beta-approve")) => Style::BetaApprove,
            None | Some(_) => return Ok(None),
        };
        toks.next_token()?;
        let team = if style != Style::BetaApprove {
            if let Some(Token::Word(team)) = toks.next_token()? {
                team.to_owned()
            } else {
                return Err(toks.error(ParseError::NoTeam));
            }
        } else {
            String::new()
        };
        if let Some(Token::Dot) | Some(Token::EndOfLine) = toks.peek_token()? {
            toks.next_token()?;
            *input = toks;
            return Ok(Some(NominateCommand { team, style }));
        } else {
            return Err(toks.error(ParseError::ExpectedEnd));
        }
    }
}

#[cfg(test)]
fn parse<'a>(input: &'a str) -> Result<Option<NominateCommand>, Error<'a>> {
    let mut toks = Tokenizer::new(input);
    Ok(NominateCommand::parse(&mut toks)?)
}

#[test]
fn test_1() {
    assert_eq!(
        parse("nominate compiler."),
        Ok(Some(NominateCommand {
            team: "compiler".into(),
            style: Style::Decision,
        }))
    );
}

#[test]
fn test_2() {
    assert_eq!(
        parse("beta-nominate compiler."),
        Ok(Some(NominateCommand {
            team: "compiler".into(),
            style: Style::Beta,
        }))
    );
}

#[test]
fn test_3() {
    use std::error::Error;
    assert_eq!(
        parse("nominate foo foo")
            .unwrap_err()
            .source()
            .unwrap()
            .downcast_ref(),
        Some(&ParseError::ExpectedEnd),
    );
}

#[test]
fn test_4() {
    use std::error::Error;
    assert_eq!(
        parse("nominate")
            .unwrap_err()
            .source()
            .unwrap()
            .downcast_ref(),
        Some(&ParseError::NoTeam),
    );
}
