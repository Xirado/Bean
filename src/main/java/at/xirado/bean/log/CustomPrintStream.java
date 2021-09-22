package at.xirado.bean.log;

import org.apache.commons.io.output.WriterOutputStream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jline.reader.LineReader;

import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

public class CustomPrintStream
{
    public static PrintStream printStream = null;

    public static PrintStream getPrintStream()
    {
        if (printStream == null)
        {
            printStream = new PrintStream(System.out)
            {

                @Override
                public void write(int b)
                {
                    if (Shell.reader.isReading())
                    {
                        Shell.reader.callWidget(LineReader.CLEAR);
                        Shell.reader.getTerminal().writer().write(b);
                        Shell.reader.callWidget(LineReader.REDRAW_LINE);
                        Shell.reader.callWidget(LineReader.REDISPLAY);
                        Shell.reader.getTerminal().writer().flush();
                    } else
                    {
                        Shell.reader.getTerminal().writer().write(b);
                    }

                }

                @Override
                public void write(@NotNull byte[] buf, int off, int len)
                {
                    if (Shell.reader.isReading())
                    {
                        String text = new String(buf, StandardCharsets.UTF_8);
                        char[] chars = text.toCharArray();
                        Shell.reader.callWidget(LineReader.CLEAR);
                        Shell.reader.getTerminal().writer().write(chars, off, len);
                        Shell.reader.callWidget(LineReader.REDRAW_LINE);
                        Shell.reader.callWidget(LineReader.REDISPLAY);
                        Shell.reader.getTerminal().writer().flush();
                    } else
                    {
                        String text = new String(buf, StandardCharsets.UTF_8);
                        char[] chars = text.toCharArray();
                        Shell.reader.getTerminal().writer().write(chars, off, len);
                    }

                }

                @Override
                public void print(boolean b)
                {
                    if (Shell.reader.isReading())
                    {
                        Shell.reader.callWidget(LineReader.CLEAR);
                        Shell.reader.getTerminal().writer().print(b);
                        Shell.reader.callWidget(LineReader.REDRAW_LINE);
                        Shell.reader.callWidget(LineReader.REDISPLAY);
                        Shell.reader.getTerminal().writer().flush();
                    } else
                    {
                        Shell.reader.getTerminal().writer().print(b);
                    }

                }

                @Override
                public void print(char c)
                {
                    if (Shell.reader.isReading())
                    {
                        Shell.reader.callWidget(LineReader.CLEAR);
                        Shell.reader.getTerminal().writer().print(c);
                        Shell.reader.callWidget(LineReader.REDRAW_LINE);
                        Shell.reader.callWidget(LineReader.REDISPLAY);
                        Shell.reader.getTerminal().writer().flush();
                    } else
                    {
                        Shell.reader.getTerminal().writer().print(c);
                    }
                }

                @Override
                public void print(int i)
                {
                    if (Shell.reader.isReading())
                    {
                        Shell.reader.callWidget(LineReader.CLEAR);
                        Shell.reader.getTerminal().writer().print(i);
                        Shell.reader.callWidget(LineReader.REDRAW_LINE);
                        Shell.reader.callWidget(LineReader.REDISPLAY);
                        Shell.reader.getTerminal().writer().flush();
                    } else
                    {
                        Shell.reader.getTerminal().writer().print(i);
                    }
                }

                @Override
                public void print(long l)
                {
                    if (Shell.reader.isReading())
                    {
                        Shell.reader.callWidget(LineReader.CLEAR);
                        Shell.reader.getTerminal().writer().print(l);
                        Shell.reader.callWidget(LineReader.REDRAW_LINE);
                        Shell.reader.callWidget(LineReader.REDISPLAY);
                        Shell.reader.getTerminal().writer().flush();
                    } else
                    {
                        Shell.reader.getTerminal().writer().print(l);
                    }
                }

                @Override
                public void print(float f)
                {
                    if (Shell.reader.isReading())
                    {
                        Shell.reader.callWidget(LineReader.CLEAR);
                        Shell.reader.getTerminal().writer().print(f);
                        Shell.reader.callWidget(LineReader.REDRAW_LINE);
                        Shell.reader.callWidget(LineReader.REDISPLAY);
                        Shell.reader.getTerminal().writer().flush();
                    } else
                    {
                        Shell.reader.getTerminal().writer().print(f);
                    }
                }

                @Override
                public void print(double d)
                {
                    if (Shell.reader.isReading())
                    {
                        Shell.reader.callWidget(LineReader.CLEAR);
                        Shell.reader.getTerminal().writer().print(d);
                        Shell.reader.callWidget(LineReader.REDRAW_LINE);
                        Shell.reader.callWidget(LineReader.REDISPLAY);
                        Shell.reader.getTerminal().writer().flush();
                    } else
                    {
                        Shell.reader.getTerminal().writer().print(d);
                    }
                }

                @Override
                public void print(@NotNull char[] s)
                {
                    if (Shell.reader.isReading())
                    {
                        Shell.reader.callWidget(LineReader.CLEAR);
                        Shell.reader.getTerminal().writer().print(s);
                        Shell.reader.callWidget(LineReader.REDRAW_LINE);
                        Shell.reader.callWidget(LineReader.REDISPLAY);
                        Shell.reader.getTerminal().writer().flush();
                    } else
                    {
                        Shell.reader.getTerminal().writer().print(s);
                    }
                }

                @Override
                public void print(@Nullable String s)
                {
                    if (Shell.reader.isReading())
                    {
                        Shell.reader.callWidget(LineReader.CLEAR);
                        Shell.reader.getTerminal().writer().print(s);
                        Shell.reader.callWidget(LineReader.REDRAW_LINE);
                        Shell.reader.callWidget(LineReader.REDISPLAY);
                        Shell.reader.getTerminal().writer().flush();
                    } else
                    {
                        Shell.reader.getTerminal().writer().print(s);
                    }
                }

                @Override
                public void print(@Nullable Object obj)
                {
                    if (Shell.reader.isReading())
                    {
                        Shell.reader.callWidget(LineReader.CLEAR);
                        Shell.reader.getTerminal().writer().print(obj);
                        Shell.reader.callWidget(LineReader.REDRAW_LINE);
                        Shell.reader.callWidget(LineReader.REDISPLAY);
                        Shell.reader.getTerminal().writer().flush();
                    } else
                    {
                        Shell.reader.getTerminal().writer().print(obj);
                    }
                }

                @Override
                public void println()
                {
                    if (Shell.reader.isReading())
                    {
                        Shell.reader.callWidget(LineReader.CLEAR);
                        Shell.reader.getTerminal().writer().println();
                        Shell.reader.callWidget(LineReader.REDRAW_LINE);
                        Shell.reader.callWidget(LineReader.REDISPLAY);
                        Shell.reader.getTerminal().writer().flush();
                    } else
                    {
                        Shell.reader.getTerminal().writer().println();
                    }
                }

                @Override
                public void println(boolean x)
                {
                    if (Shell.reader.isReading())
                    {
                        Shell.reader.callWidget(LineReader.CLEAR);
                        Shell.reader.getTerminal().writer().println(x);
                        Shell.reader.callWidget(LineReader.REDRAW_LINE);
                        Shell.reader.callWidget(LineReader.REDISPLAY);
                        Shell.reader.getTerminal().writer().flush();
                    } else
                    {
                        Shell.reader.getTerminal().writer().println(x);
                    }
                }

                @Override
                public void println(char x)
                {

                    if (Shell.reader.isReading())
                    {
                        Shell.reader.callWidget(LineReader.CLEAR);
                        Shell.reader.getTerminal().writer().println(x);
                        Shell.reader.callWidget(LineReader.REDRAW_LINE);
                        Shell.reader.callWidget(LineReader.REDISPLAY);
                        Shell.reader.getTerminal().writer().flush();
                    } else
                    {
                        Shell.reader.getTerminal().writer().println(x);
                    }
                }

                @Override
                public void println(int x)
                {

                    if (Shell.reader.isReading())
                    {
                        Shell.reader.callWidget(LineReader.CLEAR);
                        Shell.reader.getTerminal().writer().println(x);
                        Shell.reader.callWidget(LineReader.REDRAW_LINE);
                        Shell.reader.callWidget(LineReader.REDISPLAY);
                        Shell.reader.getTerminal().writer().flush();
                    } else
                    {
                        Shell.reader.getTerminal().writer().println(x);
                    }
                }

                @Override
                public void println(long x)
                {
                    if (Shell.reader.isReading())
                    {
                        Shell.reader.callWidget(LineReader.CLEAR);
                        Shell.reader.getTerminal().writer().println(x);
                        Shell.reader.callWidget(LineReader.REDRAW_LINE);
                        Shell.reader.callWidget(LineReader.REDISPLAY);
                        Shell.reader.getTerminal().writer().flush();
                    } else
                    {
                        Shell.reader.getTerminal().writer().println(x);
                    }
                }

                @Override
                public void println(float x)
                {
                    if (Shell.reader.isReading())
                    {
                        Shell.reader.callWidget(LineReader.CLEAR);
                        Shell.reader.getTerminal().writer().println(x);
                        Shell.reader.callWidget(LineReader.REDRAW_LINE);
                        Shell.reader.callWidget(LineReader.REDISPLAY);
                        Shell.reader.getTerminal().writer().flush();
                    } else
                    {
                        Shell.reader.getTerminal().writer().println(x);
                    }
                }

                @Override
                public void println(double x)
                {
                    if (Shell.reader.isReading())
                    {
                        Shell.reader.callWidget(LineReader.CLEAR);
                        Shell.reader.getTerminal().writer().println(x);
                        Shell.reader.callWidget(LineReader.REDRAW_LINE);
                        Shell.reader.callWidget(LineReader.REDISPLAY);
                        Shell.reader.getTerminal().writer().flush();
                    } else
                    {
                        Shell.reader.getTerminal().writer().println(x);
                    }
                }

                @Override
                public void println(@NotNull char[] x)
                {
                    if (Shell.reader.isReading())
                    {
                        Shell.reader.callWidget(LineReader.CLEAR);
                        Shell.reader.getTerminal().writer().println(x);
                        Shell.reader.callWidget(LineReader.REDRAW_LINE);
                        Shell.reader.callWidget(LineReader.REDISPLAY);
                        Shell.reader.getTerminal().writer().flush();
                    } else
                    {
                        Shell.reader.getTerminal().writer().println(x);
                    }
                }

                @Override
                public void println(@Nullable String x)
                {

                    if (Shell.reader.isReading())
                    {
                        Shell.reader.callWidget(LineReader.CLEAR);
                        Shell.reader.getTerminal().writer().println(x);
                        Shell.reader.callWidget(LineReader.REDRAW_LINE);
                        Shell.reader.callWidget(LineReader.REDISPLAY);
                        Shell.reader.getTerminal().writer().flush();
                    } else
                    {
                        Shell.reader.getTerminal().writer().println(x);
                    }
                }

                @Override
                public void println(@Nullable Object x)
                {
                    if (Shell.reader.isReading())
                    {
                        Shell.reader.callWidget(LineReader.CLEAR);
                        Shell.reader.getTerminal().writer().println(x);
                        Shell.reader.callWidget(LineReader.REDRAW_LINE);
                        Shell.reader.callWidget(LineReader.REDISPLAY);
                        Shell.reader.getTerminal().writer().flush();
                    } else
                    {
                        Shell.reader.getTerminal().writer().println(x);
                    }
                }

                @Override
                public PrintStream printf(@NotNull String format, Object... args)
                {
                    PrintWriter ps;
                    if (Shell.reader.isReading())
                    {
                        Shell.reader.callWidget(LineReader.CLEAR);
                        ps = Shell.reader.getTerminal().writer().printf(format, args);
                        Shell.reader.callWidget(LineReader.REDRAW_LINE);
                        Shell.reader.callWidget(LineReader.REDISPLAY);
                        Shell.reader.getTerminal().writer().flush();
                    } else
                    {
                        ps = Shell.reader.getTerminal().writer().printf(format, args);
                    }
                    OutputStream os = new WriterOutputStream(ps);
                    return new PrintStream(os);
                }
            };
        }

        return printStream;
    }
}
