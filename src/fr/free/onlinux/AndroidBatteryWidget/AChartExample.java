package fr.free.onlinux.AndroidBatteryWidget;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;

import org.achartengine.ChartFactory;
import org.achartengine.model.CategorySeries;
import org.achartengine.renderer.DefaultRenderer;
import org.achartengine.renderer.SimpleSeriesRenderer;

public class AChartExample {

	public Intent execute(Context context) {
		int[] colors = new int[] { Color.RED, Color.YELLOW, Color.BLUE };
		DefaultRenderer renderer = buildCategoryRenderer(colors);
		 
		CategorySeries categorySeries = new CategorySeries("Vehicles Chart");
		categorySeries.add("cars ", 30);
		categorySeries.add("trucks", 20);
		categorySeries.add("bikes ", 60);
		 
		return ChartFactory.getPieChartIntent(context, categorySeries, renderer, "AchartExample");
		}
	
	protected DefaultRenderer buildCategoryRenderer(int[] colors) {
		DefaultRenderer renderer = new DefaultRenderer();
		for (int color : colors) {
		SimpleSeriesRenderer r = new SimpleSeriesRenderer();
		r.setColor(color);
		renderer.addSeriesRenderer(r);
		}
		return renderer;
		}
}
