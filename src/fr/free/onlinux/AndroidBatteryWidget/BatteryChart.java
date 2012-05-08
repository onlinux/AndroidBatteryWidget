package fr.free.onlinux.AndroidBatteryWidget;

import java.util.Date;


import org.achartengine.ChartFactory;
import org.achartengine.chart.PointStyle;
import org.achartengine.chart.TimeChart;

import org.achartengine.model.TimeSeries;
import org.achartengine.model.XYMultipleSeriesDataset;

import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;
import org.achartengine.util.MathHelper;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Paint.Align;
import android.util.Log;

public class BatteryChart {
	private final static String TAG = "BATCHART";
	public final static String[] sStatus = { "Unknown", "Status unknown", "Charging", "Discharging", "Not charging", "Full"};
	public final static String[] sPlugged = {"UNPLUGGED", "PLUGGED_AC", "PLUGGED_USB"};
	
	public Intent execute(Context context) {
		Log.i( TAG, "---------- execute: " + context );
			//XYMultipleSeriesRenderer renderer = getDemoRenderer();			 
			return ChartFactory.getTimeChartIntent(context, getDateDataset(context), getRenderer(), "d/M HH:mm", "Battery level");
		
		}
	
	public String getName() {
			    return "Battery level & discharge charts";
	}
	
	public double arrondi(double val) { 
		return (Math.floor(val*100+0.5))/100; 
	} 
	/***********************************************************************
	 * getDateDataset
	 * 
	 ***********************************************************************/
	private XYMultipleSeriesDataset getDateDataset( Context context) {
		    XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
		    String postVal = "";
		    Date mDate;
		    int highestId  = -1;
	  		int counter    = 0;

	  		long rectime, prectime = -1 ;
	  		int level=0;
	  		int plevel = 0;
	  		int  plugged = -1;
	  		double discharge=0f;
			float ec = 0; //écart type
	  		
			DBHelper db = new DBHelper( context );
			Cursor c = db.read();
			c.moveToFirst();
			TimeSeries series = new TimeSeries("Battery");
			TimeSeries series_usb = new TimeSeries("USB");
			TimeSeries series_ac = new TimeSeries("AC");
			TimeSeries series_discharge = new TimeSeries("Discharge");
			
	  		while( !c.isAfterLast() )
	  		{
	  			int  id   = c.getInt(  c.getColumnIndex( "id"      ) );
	  			rectime   = c.getLong( c.getColumnIndex( "rtime" ) );
	  			level     = c.getInt(  c.getColumnIndex( "level"   ) );
	  			//status    = c.getInt(  c.getColumnIndex( "status"   ) );
	  			plugged   = c.getInt(  c.getColumnIndex("plugged"   ) );
	  			
	  			if (level != plevel) { // on ignore les enregistrements successifs avec 'level' identiques
		  			++counter;
		  			if (prectime > 0) {
		  				/*Calcul du temps ( en minutes) de décharge pour 1% de capacité de batterie*/
		  				ec = (rectime - prectime) / 1000 ;
		  				if (ec >0) discharge= arrondi(3600/ec);
		  				
		  				if (plugged ==1 || plugged ==2) discharge = arrondi(3600/ec * -1); //MathHelper.NULL_VALUE;
		  			}
		  			prectime = rectime;
		  			plevel = level;
		  			
		  			if( id > highestId ) highestId = id;
		  			mDate = new Date(rectime);
		  			postVal = id +" "+ counter + " " + rectime + " " +mDate+" "+level+"%"+  " " + ec + " mn" + " "+ discharge;
		  			Log.i( TAG, "BAT ---------- Record: " + postVal );
		  			
	  				series.add(mDate, level);
	  				series_discharge.add(mDate, discharge);
	  			
	  				if ( plugged == 1 ) series_ac.add(mDate, 0);
	  				else if ( plugged == 2 ) series_usb.add(mDate, 0);
	  			}
	  			c.moveToNext();
	  		}
	  		dataset.addSeries(series);
	  		dataset.addSeries(series_discharge);
	  		dataset.addSeries(series_usb);
	  		dataset.addSeries(series_ac);
	  		
	  		Log.i( TAG, "---------- getCount: " + c.getCount() );
	  		c.close();  // close cursor
		    db.close(); // close database 
		    return dataset;
		  }
	  
	private XYMultipleSeriesRenderer getRenderer() {
		   // TimeChart.DAY  The number of milliseconds in a day.
		  	
		    XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer(2);

		    renderer.setAxisTitleTextSize(16);
		    renderer.setChartTitleTextSize(20);
		    renderer.setLabelsTextSize(15);
		    renderer.setLegendTextSize(15);
		    renderer.setPointSize(5f);
		    renderer.setMargins(new int[] { 20, 30, 15, 20 });
		    renderer.setZoomButtonsVisible(true);
		    
		    renderer.setYTitle("Hours", 1);
		    renderer.setYAxisAlign(Align.RIGHT, 1);
		    renderer.setYLabelsAlign(Align.LEFT, 1);
		    
		    long value = new Date().getTime() - 1 * TimeChart.DAY;
		    renderer.setXAxisMin(value); // 
		    
		    renderer.setYAxisMin(0);
		    renderer.setYAxisMax(100);
		    renderer.setAxesColor(Color.GRAY);
		    renderer.setLabelsColor(Color.WHITE);
		    renderer.setXLabels(10);
            renderer.setYLabels(20); // every 10%
            renderer.setShowGrid(true);

		    renderer.setChartTitle(getName());
		    renderer.setXTitle("Time \u00BB");
		    renderer.setYTitle("Discharge %/h / Battery (%) \u00BB");
		    renderer.setAntialiasing(true);
		    renderer.setMarginsColor(Color.BLACK);
		    //renderer.setDisplayChartValues(false);
		    //SimpleSeriesRenderer.setDisplayChartValues();
		    
		    XYSeriesRenderer rb = new XYSeriesRenderer();
		    rb.setDisplayChartValues(false);
		    rb.setColor(Color.RED);
		    rb.setLineWidth(3.0f);
		    rb.setFillPoints(true);
		    rb.setFillBelowLine(true);
		    rb.setFillBelowLineColor(0x44ff0000);
		    rb.setPointStyle(PointStyle.POINT);
		    renderer.addSeriesRenderer(rb);
		    
		    // Discharge series
		    XYSeriesRenderer rd = new XYSeriesRenderer();
		    rd.setDisplayChartValues(true);
		    rd.setColor(Color.YELLOW);
		    rd.setPointStyle(PointStyle.CIRCLE);
		    rd.setLineWidth(0f);
		    rd.setFillPoints(true);
		    rd.setFillBelowLine(false);
		    //rd.setFillBelowLineColor(0x44ff0000);
		    renderer.addSeriesRenderer(rd);
		    
		    XYSeriesRenderer rusb = new XYSeriesRenderer();
		    rusb.setPointStyle(PointStyle.SQUARE);
		    rusb.setColor(Color.GREEN);
		    rusb.setFillPoints(true);
		    rusb.setLineWidth(-1.0f);
		    renderer.addSeriesRenderer(rusb);
		    
		    XYSeriesRenderer rc = new XYSeriesRenderer();
		    rc.setColor(Color.BLUE);
		    rc.setPointStyle(PointStyle.SQUARE);
		    rc.setFillPoints(true);
		    rc.setLineWidth(-1.0f);
		    renderer.addSeriesRenderer(rc);
		    




		    return renderer;
		  }
	  	  
}
