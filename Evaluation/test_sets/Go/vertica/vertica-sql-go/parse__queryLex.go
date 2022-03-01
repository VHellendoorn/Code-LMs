package parse

import (
	"strings"
	"unicode"
	"unicode/utf8"
)

// Copyright (c) 2020-2022 Micro Focus or one of its affiliates.

// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at

//    http://www.apache.org/licenses/LICENSE-2.0

// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

// SubstitutePosParam is called by the lexer when a '?' rune is encountered
// outside a string. The return value is substituted for the placeholder.
// This can be used to emulate server side binding but should be done with
// extreme caution due to the risk of SQL injection.
type SubstitutePosParam func() string

// by default leave the positional arguments alone
func defaultPosSubstitution() string {
	return "?"
}

// OnNamedParam is called when a value such as @queryParam is encountered.
// This can be used to associate a placeholder in the SQL to an index. The
// placeholders are encountered in the order they appear in the string.
type OnNamedParam func(name string)

// by default just ignore the named parameters
func defaultNamedCallback(name string) {

}

const eof = -1

// Lexer loosely breaks a SQL query down into chunks that
// the Vertica package cares about, offering opportunities for substitution.
type Lexer struct {
	input        string
	pos          int
	start        int
	width        int
	onNamed      OnNamedParam
	onPositional SubstitutePosParam
	output       strings.Builder
}

// LexOption is a function that sets a preference on the lexer
type LexOption func(*Lexer)

// WithPositionalSubstitution sets the optional positional substitution callback
func WithPositionalSubstitution(cb SubstitutePosParam) LexOption {
	return func(l *Lexer) {
		l.onPositional = cb
	}
}

// WithNamedCallback sets the optional named parameter callback
func WithNamedCallback(cb OnNamedParam) LexOption {
	return func(l *Lexer) {
		l.onNamed = cb
	}
}

// LexOptions converts an aritrary number of options into one function
// for easier handling
func LexOptions(opts ...LexOption) LexOption {
	return func(l *Lexer) {
		for _, opt := range opts {
			opt(l)
		}
	}
}

func defaultOptions() LexOption {
	return func(l *Lexer) {
		l.onNamed = defaultNamedCallback
		l.onPositional = defaultPosSubstitution
	}
}

// Lex through a given query, optionally substituting some values in the string
func Lex(query string, options ...LexOption) string {
	l := &Lexer{
		input:  query,
		output: strings.Builder{},
	}
	LexOptions(defaultOptions(), LexOptions(options...))(l)
	l.run()
	return l.output.String()
}

func (l *Lexer) run() {
	for state := lexQuery; state != nil; {
		state = state(l)
	}
}
func (l *Lexer) skipUntil(val rune) {
	for {
		next := l.next()
		if next == val || next == eof {
			return
		}
	}
}

func (l *Lexer) consumeIdent() {
	for !l.done() && !l.isEndIdent(l.next()) {
	}
}

func (l *Lexer) isEndIdent(r rune) bool {
	shouldEnd := unicode.IsSpace(r) || strings.ContainsRune(",)", r)
	if shouldEnd {
		l.backup()
	}
	return shouldEnd
}

func (l *Lexer) next() rune {
	if l.done() {
		l.width = 0
		return eof
	}
	r, w := utf8.DecodeRuneInString(l.input[l.pos:])
	l.width = w
	l.pos += l.width
	return r
}

func (l *Lexer) backup() {
	l.pos -= l.width
}

func (l *Lexer) current() rune {
	r, _ := utf8.DecodeRuneInString(l.input[l.pos:])
	return r
}

func (l *Lexer) peek() rune {
	r := l.next()
	l.backup()
	return r
}

func (l *Lexer) done() bool {
	return l.pos >= len(l.input)
}

func (l *Lexer) writeChunk() {
	l.output.WriteString(l.input[l.start:l.pos])
	l.start = l.pos
}

type stateFunc func(l *Lexer) stateFunc

func lexQuery(l *Lexer) stateFunc {
	for r := l.current(); r != eof; r = l.next() {
		if r == '-' && l.peek() == '-' {
			return lexComment
		}

		if r == '\'' {
			return lexString
		}

		if r == '@' {
			return lexNamedParam
		}

		if r == '?' {
			return lexPositional
		}
	}
	l.writeChunk()
	return nil
}

func lexComment(l *Lexer) stateFunc {
	l.skipUntil('\n')
	l.writeChunk()
	return lexQuery
}

// lexString assumes we are starting inside the string and goes to the next ' rune
// if it was an escaped quote like 'isn''t' lexQuery will drop us right back here to finish
func lexString(l *Lexer) stateFunc {
	l.skipQuotedLiteral()
	l.writeChunk()
	return lexQuery
}
func (l *Lexer) skipQuotedLiteral() {
	for {
		next := l.next()
		if next == '\'' && l.peek() == '\'' {
			l.next()
			continue
		}
		if next == '\'' || next == eof {
			return
		}
	}
}

// lexNamedParam replaces named params like @named with a ? while calling out to a consumer
// with the encountered name, minus the @ prefix
func lexNamedParam(l *Lexer) stateFunc {
	// write everything before the @
	l.backup()
	l.writeChunk()
	l.next()
	l.start = l.pos
	// advance through the name
	l.consumeIdent()
	l.onNamed(strings.ToUpper(l.input[l.start:l.pos]))
	l.start = l.pos
	l.output.WriteRune('?')
	return lexQuery
}

// lexPositional calls out to the consumer for something to replace the ? with. By default they will
// be left in place
func lexPositional(l *Lexer) stateFunc {
	l.backup()
	l.writeChunk()
	l.output.WriteString(l.onPositional())
	l.next()
	l.start = l.pos
	return lexQuery
}
