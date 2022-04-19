package in.ac.iisc.dese.aircraft.mvp.onDeviceTraining.data.speechToTextConversion;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Arrays;

/*
 * @author Sarweshkumar C R <https://github.com/sarweshkumar47>
 */
public class SpeechToTextData implements Parcelable {

    private String date;
    private String[] text;
    private String time;
    private int dateVisibilityState;

    public SpeechToTextData(String[] text, String date, String time, int dateVisibilityState) {
        this.text = text;
        //this.text[1] = text[1];
        this.date = date;
        this.time = time;
        this.dateVisibilityState = dateVisibilityState;
    }

    private SpeechToTextData(Parcel in) {
        date = in.readString();
        text[0] = in.readString();
        text[1] = in.readString();
        text[2] = in.readString();
        //text[3] = in.readString();
        time = in.readString();
        dateVisibilityState = in.readInt();
    }

    public static final Creator<SpeechToTextData> CREATOR = new Creator<SpeechToTextData>() {
        @Override
        public SpeechToTextData createFromParcel(Parcel in) {
            return new SpeechToTextData(in);
        }

        @Override
        public SpeechToTextData[] newArray(int size) {
            return new SpeechToTextData[size];
        }
    };

    public String getText() {
        return text[0];
    }

    public String Text() {
        String result = "SNR = ";
        return result + text[2];
    }

    /*public String setText() {
        String result = "Score = ";
        return result + text[1];
    }*/

    /*public String spellcorrectText() {
        String result = "SNR = ";
        return text[3];
    }*/

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    public int getDateVisibilityState() {
        return dateVisibilityState;
    }

    public void setDateVisibilityState(int dateVisibilityState) {
        this.dateVisibilityState = dateVisibilityState;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(date);
        dest.writeString(Arrays.toString(text));
        dest.writeString(time);
        dest.writeInt(dateVisibilityState);
    }

    @Override
    public String toString() {
        return "SpeechToTextData{" +
                "date='" + date + '\'' +
                ", text='" + text + '\'' +
                ", time='" + time + '\'' +
                ", dateVisibilityState=" + dateVisibilityState +
                '}';
    }
}