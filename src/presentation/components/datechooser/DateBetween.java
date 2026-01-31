package presentation.components.datechooser;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

public class DateBetween {
	public Date getFromDate() {
		return fromDate;
	}

	public void setFromDate(Date fromDate) {
		this.fromDate = fromDate;
	}

	public Date getToDate() {
		return toDate;
	}

	public void setToDate(Date toDate) {
		this.toDate = toDate;
	}

	public void setLast28Days() {
		LocalDateTime now = LocalDateTime.now();
		this.toDate = Date.from(now.atZone(ZoneId.systemDefault()).toInstant());
		this.fromDate = Date.from(now.minusDays(28).atZone(ZoneId.systemDefault()).toInstant());
	}

	public LocalDateTime getToLocalDateTime() {
		return toDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
	}
	public LocalDateTime getFromLocalDateTime() {
		return fromDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
	}

	public DateBetween(Date fromDate, Date toDate) {
		this.fromDate = fromDate;
		this.toDate = toDate;
	}

	public DateBetween() {
	}

	private Date fromDate;
	private Date toDate;

	public void fixDate() {
		if (fromDate.compareTo(toDate) == 1) {
			Date tempDate = fromDate;
			this.fromDate = toDate;
			this.toDate = tempDate;
		}
	}
}
