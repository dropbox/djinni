/**
  * Copyright 2014 Dropbox, Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */

package djinni

import java.io.Writer

package object writer {

class IndentWriter(out: Writer, indent: String = "    ", startIndent: String = "") {
  private var startOfLine = true
  private var currentIndent: String = startIndent

  def w(s: String): IndentWriter = {
    if (startOfLine) {
      out.write(currentIndent)
      startOfLine = false
    }
    out.write(s)
    this
  }

  def wl: IndentWriter = {
    out.write('\n')
    startOfLine = true
    this
  }

  def wl(s: String): IndentWriter = {
    w(s)
    wl
    this
  }

  def wlOutdent(s: String): IndentWriter = {
    decrease
    wl(s)
    increase
    this
  }

  def increase() {
    currentIndent += indent
  }

  def decrease() {
    assert(currentIndent.length > startIndent.length)
    currentIndent = currentIndent.substring(0, currentIndent.length - indent.length)
  }

  def nested(f: => Unit): Unit = nested_(1, f)

  def nestedN(amount: Int): ((=> Unit) => Unit) = nested_(amount, _)

  private def nested_(amount: Int, f: => Unit) {
    for (i <- 0 until amount) increase()
    f
    for (i <- 0 until amount) decrease()
  }

  def bracedEnd(end: String)(f: => Unit) {
    if (startOfLine) {
      wl("{")
    } else {
      wl(" {")
    }
    nested(f)
    wl(s"}$end")
  }

  def braced = bracedEnd("")(_)

  def bracedSemi = bracedEnd(";")(_)
}

}
