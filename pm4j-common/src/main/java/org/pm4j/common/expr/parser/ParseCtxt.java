package org.pm4j.common.expr.parser;

import org.apache.commons.lang.StringUtils;
import org.pm4j.common.expr.Expression.SyntaxVersion;

/**
 * Parse context for expression context. 
 */
public class ParseCtxt {
  
  public static SyntaxVersion syntaxVersion = SyntaxVersion.VERSION_1;

	/** The text to parse. */
	private String text;

	/** The current parse position within the {@link #text}. Starts with <code>0</code>. */
	private int pos;

	/**
	 * @param text The text to parse.
	 */
	public ParseCtxt(String text) {
		this.text = StringUtils.defaultString(text);
		this.pos = 0;
	}

	/**
	 *
	 * @return <code>true</code> wenn der Parse-Zeiger ({link {@link #pos})
	 *         am Ende des Textes angelangt ist.
	 */
	public final boolean isDone() {
		return pos >= text.length();
	}

  /**
   * @param ch Das zu pruefende Zeichen.
   * @return <code>true</code> wenn es ein Leezeichen, Tab, Newline o.Ä. war.
   */
  public static boolean isSpace(char ch) {
    return Character.isSpaceChar(ch) || (ch == '\n');
  }

	/**
	 * �berspringt alle Leerzeichen ab aktueller Position.
	 */
	public final ParseCtxt skipBlanks() {
		while (!isDone()) {
			if (!isSpace(text.charAt(pos))) {
				break;
			}
			++pos;
		}
		return this;
	}

	/**
	 * @param ch The char to check if it is on the current position.
	 * @return <code>true</code> if the char is at the current position.
	 */
	public final boolean isOnChar(char ch) {
		return (!isDone()) && (text.charAt(pos) == ch);
	}

	/**
	 * Darf nur aufgerufen werden wenn {@link #isDone()} <code>false</code>
	 * liefert.
	 *
	 * @return Das aktuelle Zeichen.
	 */
	public final char currentChar() {
		return text.charAt(pos);
	}

	/**
	 * Liest ein definiertes Zeichen.
	 *
	 * @param ch
	 *            Das zu lesende Zeichen.
	 * @throws ParseException
	 *             wenn das Zeichen nicht auf der aktuellen Position ist.
	 */
	public final void readChar(char ch) {
		if (text.charAt(pos) != ch) {
			throw new ParseException(this, "Character '" + ch + "' expected.");
		}
		++pos;
	}

  public final boolean testAndReadChar(char ch) {
    if (isDone() || text.charAt(pos) != ch) {
      return false;
    }
    else {
      ++pos;
      return true;
    }
  }

	/**
	 * Liest das aktuelle Zeichen und inkrementiert die Position.
	 *
	 * @return Das gelesene Zeichen.
	 * @throws ArrayOfBoundsException
	 *             wenn {@link #isDone()}.
	 */
	public final char readCharAndAdvance() {
		return text.charAt(pos++);
	}

  public final String skipBlanksAndReadNameString() {
    int startPos = pos;
    String name = skipBlanks().readNameString();

    if (name == null) {
      pos = startPos;
    }

    return name;
  }

	/**
   * Reads a typical name, starting with a letter, containing letters, numbers and
   * underlines in the following characters.
   *
   * @return The found name string or <code>null</code> if there was none at the
   *         current position.
   */
	public final String readNameString() {
	  int startPos = pos;
	  if (!isDone() && isNameStartChar(currentChar())) {
	    ++pos;
	    while (!isDone() && isNameMiddleChar(currentChar()))
	      ++pos;

	    return text.substring(startPos, pos);
	  }
	  else {
	    return null;
	  }
  }

  /**
   * @param ch The character to test.
   * @return <code>true</code> if it is a letter or an underline.
   */
	public boolean isNameStartChar(char ch) {
	  return Character.isLetter(ch) || (ch == '_');
	}

  /**
   * @param ch The character to test.
   * @return <code>true</code> if it is a letter, underline or digit.
   */
  public boolean isNameMiddleChar(char ch) {
    return isNameStartChar(ch) || Character.isDigit(ch);
  }

	/**
	 * Liest einen definierten String.
	 *
	 * @param s
	 *            Der zu lesende String.
	 * @throws ParseException
	 *             wenn der String nicht auf der aktuellen Position ist.
	 */
	public void readString(String s) {
		if (!readOptionalString(s)) {
			throw new ParseException(this, "String '" + s + "' expected.");
		}
	}

	/**
	 * Liest einen definierten String. Wenn der String an der aktuellen Position
	 * nicht gefunden werden kann, wird der Positionszeiger auch nicht
	 * verändert.
	 *
	 * @param s
	 *            Der zu lesende String.
	 * @return <code>true</code> wenn der String wirklich gefunden und gelesen
	 *         werden konnte.
	 */
	public boolean readOptionalString(String s) {
		int sLen = s.length();

		if (text.regionMatches(pos, s, 0, sLen)) {
			pos += sLen;
			return true;
		} else {
			return false;
		}
	}

  /**
   * If the current character matches the parameter value, this parse position
   * will be advanced. Otherwise the parse position remains unchanged.
   *
   * @param ch The character to test.
   * @return <code>true</code> if the given character had the same value as the
   *         character at the current parse position.
   */
	public boolean readOptionalChar(char ch) {
    if (!isDone() && currentChar() == ch) {
      ++pos;
      return true;
    }
    else {
      return false;
    }
  }

	/**
	 * Liest alle Zeichen bis zu dem im Parameter definierten
	 * Begrenzungszeichen.
	 *
	 * @param ch
	 *            Das Begrenzungszeichen.
	 * @return Alle Zeichen vor dem Begrenzer oder alle restlichen Zeichen wenn
	 *         das Zeichen nicht mehr vorkommt.
	 */
	public final String readTill(char ch) {
		int chPos = text.indexOf(ch, pos);
		String result = "";

		if (chPos == -1) {
			result = text.substring(pos);
			pos = text.length();
		} else {
			result = text.substring(pos, chPos);
			pos = chPos;
		}

		return result;
	}

	/**
	 * @return Die aktuelle Position im Text. Beginnt bei <code>0</code>.
	 */
	public int getPos() {
		return pos;
	}

	/**
	 * @param newPos
	 *            Die neue Position.
	 */
	public void setPos(int newPos) {
		this.pos = newPos;
	}

	/**
	 * @return Der zu interpretierende Text.
	 */
	public String getText() {
		return text;
	}
	
	/**
	 * @return Die Syntax version.
	 */
	public static SyntaxVersion getSyntaxVersion()
	{
	  return syntaxVersion;
	}
	
}
