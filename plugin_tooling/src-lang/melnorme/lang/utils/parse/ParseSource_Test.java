/*******************************************************************************
 * Copyright (c) 2015, 2015 Bruno Medeiros and other Contributors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Bruno Medeiros - initial API and implementation
 *******************************************************************************/
package melnorme.lang.utils.parse;

import static melnorme.utilbox.core.Assert.AssertNamespace.assertTrue;

import java.io.StringReader;

import melnorme.lang.tests.CommonToolingTest;

import org.junit.Test;

public abstract class ParseSource_Test extends CommonToolingTest {
	
	protected final String TEST_SOURCE = "abcdef";
	
	protected ParseSource<?> parseSource;
	protected String source;
	protected int sourceIx;
	protected int lookahead;
	
	protected void init(String source) {
		this.source = source;
		this.parseSource = createParseSource(source);
		this.sourceIx = 0;
	}
	
	protected abstract ParseSource<?> createParseSource(String source);
	
	@Test
	public void test() throws Exception { test$(); }
	public void test$() throws Exception {
		doTest______();
	}
	
	protected void doTest______() throws Exception {
		init(TEST_SOURCE);
		testLookahead('a');
		
		assertTrue(parseSource.lookaheadMatches("abc", 0));
		assertTrue(parseSource.lookaheadMatches("bc", 0) == false);
		assertTrue(parseSource.lookaheadMatches("bc", 1));
		assertTrue(parseSource.lookaheadMatches("abc", TEST_SOURCE.length()) == false);
		assertTrue(parseSource.lookaheadMatches("", TEST_SOURCE.length()));
		
		init(TEST_SOURCE);
		testCharSource();
		
		init(TEST_SOURCE);
		assertTrue(parseSource.stringUntil("z").equals("abcdef"));
		assertTrue(parseSource.stringUntil("a").equals(""));
		assertTrue(parseSource.stringUntil("def").equals("abc"));
		parseSource.consumeAhead("abc");
		assertTrue(parseSource.stringUntil("def").equals(""));
		assertTrue(parseSource.stringUntil("z").equals("def"));
		
		init(TEST_SOURCE);
		assertTrue(parseSource.consumeUntil("def").equals("abc"));
		assertTrue(parseSource.lookaheadMatches("def"));
		
		init(TEST_SOURCE);
		assertTrue(parseSource.consumeUntil("de", true).equals("abc"));
		assertTrue(parseSource.lookaheadMatches("f"));
	}
	
	protected void testCharSource() throws Exception {
		
		// Test lookahead
		checkBufferedCount(parseSource, 0);
		lookahead = testLookahead(source.charAt(sourceIx));
		
		// Test lookahead(1)
		assertTrue(parseSource.lookahead(1) == source.charAt(sourceIx + 1));
		
		// Test consume with buffered
		checkBufferedCount(parseSource, 2);
		assertTrue(parseSource.consume() == lookahead); sourceIx++;
		checkBufferedCount(parseSource, 1);
		assertTrue(parseSource.consume() == source.charAt(sourceIx)); sourceIx++;
		checkBufferedCount(parseSource, 0);
		
		
		checkBufferedCount(parseSource, 0);
		lookahead = testLookahead(source.charAt(sourceIx));
		// Test consume with buffered
		checkBufferedCount(parseSource, 1);
		assertTrue(lookahead == parseSource.consume()); sourceIx++;
		
		assertTrue(lookahead == 'c');
		
		
		checkBufferedCount(parseSource, 0);
		assertTrue(parseSource.consume() == source.charAt(sourceIx));
		sourceIx++;

		while(sourceIx < source.length()) {
			int ch = testLookahead(source.charAt(sourceIx));
			assertTrue(parseSource.consume() == ch);
			sourceIx++;
		}
		
		// EOF
		testLookahead(-1);
		assertTrue(parseSource.consume() == -1);
	}

	protected void checkBufferedCount(ICharSource<?> parseSource, int expected) {
		assertTrue(parseSource.bufferedCharCount() == expected);
	}
	
	protected int testLookahead(int expected) throws Exception {
		return testLookAhead(parseSource, expected);
	}
	
	public static int testLookAhead(ICharSource<?> parseSource, int expected) throws Exception {
		int lookahead = parseSource.lookahead();
		assertTrue(lookahead == expected);
		assertTrue(lookahead == parseSource.lookahead(0));
		return lookahead;
	}
	
	@Test
	public void test_consumeDelimited() throws Exception { test_consumeDelimited$(); }
	public void test_consumeDelimited$() throws Exception {
		
		init("blah");
		assertEquals(parseSource.consumeDelimitedString('|', '#'), "blah");
		
		init("one|two|three##|four#|xxx|###|five");
		assertEquals(parseSource.consumeDelimitedString('|', '#'), "one");
		assertEquals(parseSource.consumeDelimitedString('|', '#'), "two");
		assertEquals(parseSource.consumeDelimitedString('|', '#'), "three#");
		assertEquals(parseSource.consumeDelimitedString('|', '#'), "four|xxx");
		assertEquals(parseSource.consumeDelimitedString('|', '#'), "#|five");
	}
	
	@Test
	public void testConsumeNewline() throws Exception { testConsumeNewline$(); }
	public void testConsumeNewline$() throws Exception {
		init("abc\ndef\r\nzzz");
		assertAreEqual(parseSource.stringUntilNewline(0), "abc");
		assertAreEqual(parseSource.stringUntilNewline(1), "bc");
		assertAreEqual(parseSource.stringUntilNewline(3), "");
		assertAreEqual(parseSource.consumeLine(), "abc");
		assertAreEqual(parseSource.consumeLine(), "def");
		assertAreEqual(parseSource.consumeLine(), "zzz");
		assertAreEqual(parseSource.consumeLine(), null);
	}
	
	
	/* ----------------- Actual tests ----------------- */
	
	public static class ReaderParseSource_Test extends ParseSource_Test {
		@Override
		protected ParseSource<?> createParseSource(String source) {
			return new ReaderParseSource(new StringReader(source));
		}
	}
	
	public static class StringParseSource_Test extends ParseSource_Test {
		@Override
		protected ParseSource<?> createParseSource(String source) {
			return new StringParseSource(source);
		}
		
		@Override
		protected void checkBufferedCount(ICharSource<?> parseSource, int expected) {
			assertTrue(parseSource.bufferedCharCount() == source.length() - sourceIx);
		}
	}
	
}