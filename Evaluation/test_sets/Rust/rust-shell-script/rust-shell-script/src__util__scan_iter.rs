use std::mem::discriminant;
use std::borrow::Borrow;

pub trait Newline {
    fn is_newline(&self) -> bool;
}

pub trait CmpType: Sized {
    fn cmp_type(&self, other: &Self) -> bool {
        discriminant(self) == discriminant(other)
    }
}

pub struct ScanIter<Item: Newline+CmpType> {
    items: Vec<Item>,
    line: usize,
    pos: usize,
}

impl<Item: Newline+CmpType> ScanIter<Item> {
    pub fn new(items: Vec<Item>) -> Self {
        Self { items, pos: 0, line: 1}
    }

    pub fn is_at_end(&self) -> bool {
        self.pos >= self.items.len()
    }

    pub fn line(&self) -> usize {
        self.line
    }

    pub fn advance(&mut self) {
        if !self.is_at_end() {
            if self.items[self.pos].is_newline() {
                self.line += 1;
            }
            self.pos += 1;
        }
    }

    pub fn previous(&mut self) -> &Item {
        &self.items[self.pos - 1]
    }

    pub fn peek(&self) -> &Item {
        &self.items[self.pos]
    }

    pub fn check_item<T: Borrow<Item>>(&mut self, expected: T)  -> bool {
        if self.items[self.pos].is_newline() {
            self.advance();
        }
        if self.is_at_end() { return false; }
        self.peek().cmp_type(expected.borrow())
    }

    pub fn match_item<T: Borrow<Item>>(&mut self, expected: T)  -> bool {
        if self.check_item(expected) {
            self.advance();
            true
        } else {
            false
        }
    }

    pub fn match_any_item(&mut self, expected: &[Item]) -> bool {
        for e in expected {
            if self.match_item(e) { return true; }
        }
        false
    }
}
