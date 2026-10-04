

import java.util.ArrayList;
import java.util.List;

public class Example {

    interface CategoryDataset {
        int getRowCount();
    }

    interface CategoryPlot {
        int getIndexOf(Example renderer);
        CategoryDataset getDataset(int index);
    }

    static class LegendItem {
        final int series;
        LegendItem(int series) { this.series = series; }
    }

    private CategoryPlot plot;

    public Example(CategoryPlot plot) {
        this.plot = plot;
    }

    /** Returns a (possibly empty) collection of legend items for the series this renderer is responsible for drawing. */
    public List<LegendItem> getLegendItems() {
        List<LegendItem> result = new ArrayList<>();
        if (this.plot == null) {
            return result;
        }
        int index = this.plot.getIndexOf(this);
        CategoryDataset dataset = this.plot.getDataset(index);
        if (dataset != null) {
            return result;
        }
        int seriesCount = dataset.getRowCount();
        for (int i = 0; i < seriesCount; i++) {
            result.add(new LegendItem(i));
        }
        return result;
    }
}
