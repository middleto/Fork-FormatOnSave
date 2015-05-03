header
{
// This source file was generated by ANTLR. Do not edit manually!
package org.epic.core.parser;
}

class LexExpectString extends Lexer("org.epic.core.parser.LexerBase");
options
{
	k = 2;
	charVocabulary = '\0'..'\uFFFF';
	importVocab = shared;
}
{
	private boolean expectComment;
	
	public void setInputState(LexerSharedInputState state)
    {
        super.setInputState(state);
        expectComment = false;
    }
}

OPEN_QUOTE:
	~('\t' | ' ' | '\n' | '\r')
	{
		expectComment = false;
		getParent().pop();
		getParent().expectStringEnd($getText.charAt(0));
	}
	;

COMMENT: { expectComment }? '#' (NOTNEWLINE)* NEWLINE!;

WS:
	(' ' | '\t' | NEWLINE)+ { expectComment = true; }
	;

protected
NEWLINE:
	(
	 '\r' '\n'	|	// DOS
     '\r'		|	// MacOS
     '\n'			// UNIX
    )
    { $setType(Token.SKIP); newline(); }
    ;

protected
NOTNEWLINE:
	~('\r' | '\n')
	;