package in.ac.iisc.dese.aircraft.mvp.onDeviceTraining.screens.speechToText.listItem;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import in.ac.iisc.dese.aircraft.mvp.onDeviceTraining.R;
import in.ac.iisc.dese.aircraft.mvp.onDeviceTraining.data.speechToTextConversion.SpeechToTextData;


public class SpeechToTextHistoryRecyclerViewAdapter extends RecyclerView.Adapter<SpeechToTextHistoryRecyclerViewAdapter.MyViewHolder> {

    private List<SpeechToTextData> speechToTextDataList = new ArrayList<>(1);

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.view_list_item,
                viewGroup, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder viewHolder, int i) {
        viewHolder.bindSpeechData(speechToTextDataList.get(i));
    }

    @Override
    public int getItemCount() {
        return speechToTextDataList.size();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {

        @Nullable
        @BindView(R.id.dateTextView)
        TextView dateTextView;

        @Nullable
        @BindView(R.id.speechTextView)
        TextView speechTextView;

        @Nullable
        @BindView(R.id.SNRView)
        TextView SNRView;

        @Nullable
        @BindView(R.id.ResultView)
        TextView ResultView;

        @Nullable
        @BindView(R.id.SpellCorrectResultView)
        TextView SpellCorrectResultView;

        @Nullable
        @BindView(R.id.timeTextView)
        TextView timeTextView;

        MyViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

        void bindSpeechData(SpeechToTextData speechToTextData) {
            dateTextView.setText(speechToTextData.getDate());
            dateTextView.setVisibility(speechToTextData.getDateVisibilityState());
            //speechTextView.setText(speechToTextData.getText());
            speechTextView.setText(speechToTextData.getText());
            SNRView.setText(speechToTextData.Text());
            //ResultView.setText(speechToTextData.setText());
            //SpellCorrectResultView.setText(speechToTextData.spellcorrectText());
            timeTextView.setText(speechToTextData.getTime());
        }
    }

    public void addAll(List<SpeechToTextData> list) {
        speechToTextDataList.clear();
        speechToTextDataList.addAll(list);
        notifyDataSetChanged();
    }

    public void add(SpeechToTextData speechToTextData) {
        speechToTextDataList.add(speechToTextData);
        notifyDataSetChanged();
    }
}
