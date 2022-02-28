import os
import sys
import antlr3
import testbase
import unittest


class t021hoist(testbase.ANTLRTest):
    def setUp(self):
        self.compileGrammar()
        

    def testValid1(self):
        cStream = antlr3.StringStream('enum')
        lexer = self.getLexer(cStream)
        tStream = antlr3.CommonTokenStream(lexer)
        parser = self.getParser(tStream)
        parser.enableEnum = True
        enumIs = parser.stat()

        self.assertEqual(enumIs, 'keyword')


    def testValid2(self):
        cStream = antlr3.StringStream('enum')
        lexer = self.getLexer(cStream)
        tStream = antlr3.CommonTokenStream(lexer)
        parser = self.getParser(tStream)
        parser.enableEnum = False
        enumIs = parser.stat()

        self.assertEqual(enumIs, 'ID')



if __name__ == '__main__':
    unittest.main()

