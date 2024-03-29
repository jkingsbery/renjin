/*
 * R : A Computer Language for Statistical Data Analysis
 * Copyright (C) 1995, 1996  Robert Gentleman and Ross Ihaka
 * Copyright (C) 1997--2008  The R Development Core Team
 * Copyright (C) 2003, 2004  The R Foundation
 * Copyright (C) 2010 bedatadriven
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package r.parser;

import org.junit.Test;
import r.lang.StringVector;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class ParseUtilTest {

  @Test
  public void deparseCharacterNA() {

    StringVector exp = new StringVector(StringVector.NA);

    assertThat(new ParseUtil.StringDeparser().apply(StringVector.NA), equalTo("NA_character_"));
  }

  @Test
  public void parseZeroPointZero() {
    assertThat(ParseUtil.parseDouble("0.0"), equalTo(0d));
  }

  @Test
  public void unicodeEscapes() {
    assertThat(ParseUtil.formatStringLiteral("\u0001", "NA"), equalTo("\"\\u0001\""));
    assertThat(ParseUtil.formatStringLiteral("\u00a1", "NA"), equalTo("\"\\u00a1\""));
    assertThat(ParseUtil.formatStringLiteral("\u01a1", "NA"), equalTo("\"\\u01a1\""));
    assertThat(ParseUtil.formatStringLiteral("\u1fa1", "NA"), equalTo("\"\\u1fa1\""));
  }
}
