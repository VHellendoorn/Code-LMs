"""Testsuite for TokenRewriteStream class."""

# don't care about docstrings
# pylint: disable-msg=C0111

import unittest
import antlr3
import testbase

class T1(testbase.ANTLRTest):
    def setUp(self):
        self.compileGrammar()


    def _parse(self, input):
        cStream = antlr3.StringStream(input)
        lexer = self.getLexer(cStream)
        tStream = antlr3.TokenRewriteStream(lexer)
        tStream.fillBuffer()

        return tStream


    def testInsertBeforeIndex0(self):
        tokens = self._parse("abc")
        tokens.insertBefore(0, "0")

        result = tokens.toString()
        expecting = "0abc"
        self.assertEqual(result, expecting)


    def testInsertAfterLastIndex(self):
        tokens = self._parse("abc")
        tokens.insertAfter(2, "x")

        result = tokens.toString()
        expecting = "abcx"
        self.assertEqual(result, expecting)


    def test2InsertBeforeAfterMiddleIndex(self):
        tokens = self._parse("abc")
        tokens.insertBefore(1, "x")
        tokens.insertAfter(1, "x")

        result = tokens.toString()
        expecting = "axbxc"
        self.assertEqual(result, expecting)


    def testReplaceIndex0(self):
        tokens = self._parse("abc")
        tokens.replace(0, "x")

        result = tokens.toString()
        expecting = "xbc"
        self.assertEqual(result, expecting)


    def testReplaceLastIndex(self):
        tokens = self._parse("abc")
        tokens.replace(2, "x")

        result = tokens.toString()
        expecting = "abx"
        self.assertEqual(result, expecting)


    def testReplaceMiddleIndex(self):
        tokens = self._parse("abc")
        tokens.replace(1, "x")

        result = tokens.toString()
        expecting = "axc"
        self.assertEqual(result, expecting)


    def test2ReplaceMiddleIndex(self):
        tokens = self._parse("abc")
        tokens.replace(1, "x")
        tokens.replace(1, "y")

        result = tokens.toString()
        expecting = "ayc"
        self.assertEqual(result, expecting)


    def test2ReplaceMiddleIndex1InsertBefore(self):
        tokens = self._parse("abc")
        tokens.insertBefore(0, "_")
        tokens.replace(1, "x")
        tokens.replace(1, "y")

        result = tokens.toString()
        expecting = "_ayc"
        self.assertEqual(expecting, result)


    def testReplaceThenDeleteMiddleIndex(self):
        tokens = self._parse("abc")
        tokens.replace(1, "x")
        tokens.delete(1)

        result = tokens.toString()
        expecting = "ac"
        self.assertEqual(result, expecting)


    def testInsertInPriorReplace(self):
        tokens = self._parse("abc")
        tokens.replace(0, 2, "x")
        tokens.insertBefore(1, "0")
        self.assertRaisesRegex(
            ValueError,
            (r'insert op <InsertBeforeOp@1:"0"> within boundaries of '
             r'previous <ReplaceOp@0\.\.2:"x">'),
            tokens.toString)

    def testInsertThenReplaceSameIndex(self):
        tokens = self._parse("abc")
        tokens.insertBefore(0, "0")
        tokens.replace(0, "x")  # supercedes insert at 0

        result = tokens.toString()
        expecting = "0xbc"
        self.assertEqual(result, expecting)


    def test2InsertMiddleIndex(self):
        tokens = self._parse("abc")
        tokens.insertBefore(1, "x")
        tokens.insertBefore(1, "y")

        result = tokens.toString()
        expecting = "ayxbc"
        self.assertEqual(result, expecting)


    def test2InsertThenReplaceIndex0(self):
        tokens = self._parse("abc")
        tokens.insertBefore(0, "x")
        tokens.insertBefore(0, "y")
        tokens.replace(0, "z")

        result = tokens.toString()
        expecting = "yxzbc"
        self.assertEqual(result, expecting)


    def testReplaceThenInsertBeforeLastIndex(self):
        tokens = self._parse("abc")
        tokens.replace(2, "x")
        tokens.insertBefore(2, "y")

        result = tokens.toString()
        expecting = "abyx"
        self.assertEqual(result, expecting)


    def testInsertThenReplaceLastIndex(self):
        tokens = self._parse("abc")
        tokens.insertBefore(2, "y")
        tokens.replace(2, "x")

        result = tokens.toString()
        expecting = "abyx"
        self.assertEqual(result, expecting)


    def testReplaceThenInsertAfterLastIndex(self):
        tokens = self._parse("abc")
        tokens.replace(2, "x")
        tokens.insertAfter(2, "y")

        result = tokens.toString()
        expecting = "abxy"
        self.assertEqual(result, expecting)


    def testReplaceRangeThenInsertAtLeftEdge(self):
        tokens = self._parse("abcccba")
        tokens.replace(2, 4, "x")
        tokens.insertBefore(2, "y")

        result = tokens.toString()
        expecting = "abyxba"
        self.assertEqual(result, expecting)


    def testReplaceRangeThenInsertAtRightEdge(self):
        tokens = self._parse("abcccba")
        tokens.replace(2, 4, "x")
        tokens.insertBefore(4, "y") # no effect; within range of a replace

        self.assertRaisesRegex(
            ValueError,
            (r'insert op <InsertBeforeOp@4:"y"> within boundaries of '
             r'previous <ReplaceOp@2\.\.4:"x">'),
            tokens.toString)


    def testReplaceRangeThenInsertAfterRightEdge(self):
        tokens = self._parse("abcccba")
        tokens.replace(2, 4, "x")
        tokens.insertAfter(4, "y")

        result = tokens.toString()
        expecting = "abxyba"
        self.assertEqual(result, expecting)


    def testReplaceAll(self):
        tokens = self._parse("abcccba")
        tokens.replace(0, 6, "x")

        result = tokens.toString()
        expecting = "x"
        self.assertEqual(result, expecting)


    def testReplaceSubsetThenFetch(self):
        tokens = self._parse("abcccba")
        tokens.replace(2, 4, "xyz")

        result = tokens.toString(0, 6)
        expecting = "abxyzba"
        self.assertEqual(result, expecting)


    def testReplaceThenReplaceSuperset(self):
        tokens = self._parse("abcccba")
        tokens.replace(2, 4, "xyz")
        tokens.replace(3, 5, "foo") # overlaps, error

        self.assertRaisesRegex(
            ValueError,
            (r'replace op boundaries of <ReplaceOp@3\.\.5:"foo"> overlap '
             r'with previous <ReplaceOp@2\.\.4:"xyz">'),
            tokens.toString)


    def testReplaceThenReplaceLowerIndexedSuperset(self):
        tokens = self._parse("abcccba")
        tokens.replace(2, 4, "xyz")
        tokens.replace(1, 3, "foo") # overlap, error

        self.assertRaisesRegex(
            ValueError,
            (r'replace op boundaries of <ReplaceOp@1\.\.3:"foo"> overlap '
             r'with previous <ReplaceOp@2\.\.4:"xyz">'),
            tokens.toString)


    def testReplaceSingleMiddleThenOverlappingSuperset(self):
        tokens = self._parse("abcba")
        tokens.replace(2, 2, "xyz")
        tokens.replace(0, 3, "foo")

        result = tokens.toString()
        expecting = "fooa"
        self.assertEqual(result, expecting)


    def testCombineInserts(self):
        tokens = self._parse("abc")
        tokens.insertBefore(0, "x")
        tokens.insertBefore(0, "y")
        result = tokens.toString()
        expecting = "yxabc"
        self.assertEqual(expecting, result)


    def testCombine3Inserts(self):
        tokens = self._parse("abc")
        tokens.insertBefore(1, "x")
        tokens.insertBefore(0, "y")
        tokens.insertBefore(1, "z")
        result = tokens.toString()
        expecting = "yazxbc"
        self.assertEqual(expecting, result)


    def testCombineInsertOnLeftWithReplace(self):
        tokens = self._parse("abc")
        tokens.replace(0, 2, "foo")
        tokens.insertBefore(0, "z") # combine with left edge of rewrite
        result = tokens.toString()
        expecting = "zfoo"
        self.assertEqual(expecting, result)


    def testCombineInsertOnLeftWithDelete(self):
        tokens = self._parse("abc")
        tokens.delete(0, 2)
        tokens.insertBefore(0, "z") # combine with left edge of rewrite
        result = tokens.toString()
        expecting = "z" # make sure combo is not znull
        self.assertEqual(expecting, result)


    def testDisjointInserts(self):
        tokens = self._parse("abc")
        tokens.insertBefore(1, "x")
        tokens.insertBefore(2, "y")
        tokens.insertBefore(0, "z")
        result = tokens.toString()
        expecting = "zaxbyc"
        self.assertEqual(expecting, result)


    def testOverlappingReplace(self):
        tokens = self._parse("abcc")
        tokens.replace(1, 2, "foo")
        tokens.replace(0, 3, "bar") # wipes prior nested replace
        result = tokens.toString()
        expecting = "bar"
        self.assertEqual(expecting, result)


    def testOverlappingReplace2(self):
        tokens = self._parse("abcc")
        tokens.replace(0, 3, "bar")
        tokens.replace(1, 2, "foo") # cannot split earlier replace

        self.assertRaisesRegex(
            ValueError,
            (r'replace op boundaries of <ReplaceOp@1\.\.2:"foo"> overlap '
             r'with previous <ReplaceOp@0\.\.3:"bar">'),
            tokens.toString)


    def testOverlappingReplace3(self):
        tokens = self._parse("abcc")
        tokens.replace(1, 2, "foo")
        tokens.replace(0, 2, "bar") # wipes prior nested replace
        result = tokens.toString()
        expecting = "barc"
        self.assertEqual(expecting, result)


    def testOverlappingReplace4(self):
        tokens = self._parse("abcc")
        tokens.replace(1, 2, "foo")
        tokens.replace(1, 3, "bar") # wipes prior nested replace
        result = tokens.toString()
        expecting = "abar"
        self.assertEqual(expecting, result)


    def testDropIdenticalReplace(self):
        tokens = self._parse("abcc")
        tokens.replace(1, 2, "foo")
        tokens.replace(1, 2, "foo") # drop previous, identical
        result = tokens.toString()
        expecting = "afooc"
        self.assertEqual(expecting, result)


    def testDropPrevCoveredInsert(self):
        tokens = self._parse("abc")
        tokens.insertBefore(1, "foo")
        tokens.replace(1, 2, "foo") # kill prev insert
        result = tokens.toString()
        expecting = "afoofoo"
        self.assertEqual(expecting, result)


    def testLeaveAloneDisjointInsert(self):
        tokens = self._parse("abcc")
        tokens.insertBefore(1, "x")
        tokens.replace(2, 3, "foo")
        result = tokens.toString()
        expecting = "axbfoo"
        self.assertEqual(expecting, result)


    def testLeaveAloneDisjointInsert2(self):
        tokens = self._parse("abcc")
        tokens.replace(2, 3, "foo")
        tokens.insertBefore(1, "x")
        result = tokens.toString()
        expecting = "axbfoo"
        self.assertEqual(expecting, result)


    def testInsertBeforeTokenThenDeleteThatToken(self):
        tokens = self._parse("abc")
        tokens.insertBefore(2, "y")
        tokens.delete(2)
        result = tokens.toString()
        expecting = "aby"
        self.assertEqual(expecting, result)


class T2(testbase.ANTLRTest):
    def setUp(self):
        self.compileGrammar('t048rewrite2.g')


    def _parse(self, input):
        cStream = antlr3.StringStream(input)
        lexer = self.getLexer(cStream)
        tStream = antlr3.TokenRewriteStream(lexer)
        tStream.fillBuffer()

        return tStream


    def testToStringStartStop(self):
        # Tokens: 0123456789
        # Input:  x = 3 * 0
        tokens = self._parse("x = 3 * 0;")
        tokens.replace(4, 8, "0") # replace 3 * 0 with 0

        result = tokens.toOriginalString()
        expecting = "x = 3 * 0;"
        self.assertEqual(expecting, result)

        result = tokens.toString()
        expecting = "x = 0;"
        self.assertEqual(expecting, result)

        result = tokens.toString(0, 9)
        expecting = "x = 0;"
        self.assertEqual(expecting, result)

        result = tokens.toString(4, 8)
        expecting = "0"
        self.assertEqual(expecting, result)


    def testToStringStartStop2(self):
        # Tokens: 012345678901234567
        # Input:  x = 3 * 0 + 2 * 0
        tokens = self._parse("x = 3 * 0 + 2 * 0;")

        result = tokens.toOriginalString()
        expecting = "x = 3 * 0 + 2 * 0;"
        self.assertEqual(expecting, result)

        tokens.replace(4, 8, "0") # replace 3 * 0 with 0
        result = tokens.toString()
        expecting = "x = 0 + 2 * 0;"
        self.assertEqual(expecting, result)

        result = tokens.toString(0, 17)
        expecting = "x = 0 + 2 * 0;"
        self.assertEqual(expecting, result)

        result = tokens.toString(4, 8)
        expecting = "0"
        self.assertEqual(expecting, result)

        result = tokens.toString(0, 8)
        expecting = "x = 0"
        self.assertEqual(expecting, result)

        result = tokens.toString(12, 16)
        expecting = "2 * 0"
        self.assertEqual(expecting, result)

        tokens.insertAfter(17, "// comment")
        result = tokens.toString(12, 18)
        expecting = "2 * 0;// comment"
        self.assertEqual(expecting, result)

        result = tokens.toString(0, 8) # try again after insert at end
        expecting = "x = 0"
        self.assertEqual(expecting, result)


if __name__ == '__main__':
    unittest.main()
