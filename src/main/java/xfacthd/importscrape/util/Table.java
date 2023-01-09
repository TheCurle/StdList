package xfacthd.importscrape.util;

import java.util.ArrayList;
import java.util.List;

public final class Table
{
    private final int columns;
    private final int[] colWidths;
    private final String[] header;
    private final List<String[]> content = new ArrayList<>();

    public Table(int columns)
    {
        this.columns = columns;
        this.colWidths = new int[columns];
        this.header = new String[columns];
    }

    public void setHeader(String... cols)
    {
        validateWidth(cols);
        adaptPrintWidth(cols);
        System.arraycopy(cols, 0, header, 0, columns);
    }

    public void addLine(String... cols)
    {
        validateWidth(cols);
        adaptPrintWidth(cols);
        content.add(cols);
    }

    public String print()
    {
        StringBuilder builder = new StringBuilder();

        StringBuilder headerLine = new StringBuilder("| ");
        StringBuilder sepLine = new StringBuilder("|");
        for (int i = 0; i < columns; i++)
        {
            headerLine.append(printComponent(i, header[i], " ")).append(" | ");
            sepLine.append(":").append(printComponent(i, "", "-")).append("-|");
        }
        builder.append(headerLine.toString().trim()).append("\n");
        builder.append(sepLine.toString().trim()).append("\n");

        for (String[] line : content)
        {
            StringBuilder contentLine = new StringBuilder("| ");
            for (int i = 0; i < columns; i++)
            {
                contentLine.append(printComponent(i, line[i], " ")).append(" | ");
            }
            builder.append(contentLine.toString().trim()).append("\n");
        }

        return builder.toString();
    }



    private void adaptPrintWidth(String[] cols)
    {
        for (int i = 0; i < columns; i++)
        {
            colWidths[i] = Math.max(colWidths[i], cols[i].length());
        }
    }

    private String printComponent(int colIdx, String col, String fill)
    {
        return col + fill.repeat(colWidths[colIdx] - col.length());
    }

    private void validateWidth(String[] cols)
    {
        if (cols == null || cols.length != columns)
        {
            int count = cols == null ? 0 : cols.length;
            throw new IllegalArgumentException("Invalid column count " + count + ", expected " + columns);
        }
    }
}
