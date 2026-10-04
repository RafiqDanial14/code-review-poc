// Defects4J Chart-1 (JFreeChart, AbstractCategoryItemRenderer.getLegendItems)
// Extracted buggy method, minimized: JFreeChart types replaced by small stubs.
import java.util.ArrayList;
import java.util.List;

public class Chart1 {

    interface CategoryDataset {
        int getRowCount();
    }

    interface CategoryPlot {
        int getIndexOf(Chart1 renderer);
        CategoryDataset getDataset(int index);
    }

    static class LegendItem {
        final int series;
        LegendItem(int series) { this.series = series; }
    }

    private CategoryPlot plot;

    public Chart1(CategoryPlot plot) {
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
        if (dataset == null) {
            return result;
        }
        int seriesCount = dataset.getRowCount();
        for (int i = 0; i < seriesCount; i++) {
            result.add(new LegendItem(i));
        }
        return result;
    }
}
