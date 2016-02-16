package hmm;

import java.io.Serializable;
import java.util.Date;
import be.ac.ulg.montefiore.run.jahmm.ObservationVector;

public class Sensex extends ObservationVector implements Serializable, Comparable<Sensex> {
	private Long id;
    private Date date;
    private Double open;
    private Double low;
    private Double high;
    private Double close;

    public Sensex() {
        super(4);
    }

    public Sensex(Long id) {
        this();
        this.id = id;
    }

    public Sensex(Date date, Double open, Double low, Double high, Double close) {
        this(new double[] { open, low, high, close });
        setDate(date);
    }

    public Sensex(double[] value) {
        super(value);
        setDate(null);
        setOpen(value[0]);
        setLow(value[1]);
        setHigh(value[2]);
        setClose(value[3]);
    }

    Sensex(Sensex get)
    {
        this();
        this.close = get.close;
        this.date = get.date;
        this.high = get.high;
        this.id = get.id;
        this.low = get.low;
        this.open = get.open;
    }
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String toString() {
    	return "[FTSE on " + date + ":(" + open + ", " + low + ", " + high + ", " + close + ")]";
    }

    public int compareTo(Sensex o) {
        if (id != null && o.id != null) {
            return id.compareTo(o.id);
        }
        if (date != null && o.date != null) {
            return date.compareTo(o.date);
        }
        if (open != null && o.open != null) {
            return open.compareTo(o.open);
        }
        if (low != null && o.low != null) {
            return low.compareTo(o.low);
        }
        if (high != null && o.high != null) {
            return high.compareTo(o.high);
        }
        if (close != null && o.close != null) {
            return close.compareTo(o.close);
        }
        return 0;
    }

    public int hashCode() {
        if (id == null) {
            return Integer.MIN_VALUE;
        }
        return this.id.hashCode();
    }

    public boolean equals(Object obj) {
		if (obj == null) {
			return false;
	    }
        if (!(obj instanceof Sensex)) {
            return false;
        }
        Sensex o = (Sensex) obj;
        return id.equals(o.id) || date.equals(o.date);
    }

    public Date getDate() {
        return date;
    }
      
    public void setDate(Date date) {
        this.date = date;
    }

    public Double getOpen() {
        return open;
    }

    public void setOpen(Double open) {
        this.open = open;
    }

    public Double getLow() {
        return low;
    }

    public void setLow(Double low) {
        this.low = low;
    }

    public Double getHigh() {
        return high;
    }

    public void setHigh(Double high) {
        this.high = high;
    }

    public Double getClose() {
        return close;
    }

    public void setClose(Double close) {
    	this.close = close;
    }
    
    public void find(int flag) {
	    randomRange randomRangeInstance = new randomRange();
	    double prediction;
	    double error = 0.00024667;
	    System.out.println(this.open);
	    prediction = this.open*error + this.open;
	    System.out.println(prediction);
    }
}