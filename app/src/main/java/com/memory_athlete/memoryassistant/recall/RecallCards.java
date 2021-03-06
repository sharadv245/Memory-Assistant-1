package com.memory_athlete.memoryassistant.recall;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.memory_athlete.memoryassistant.Helper;
import com.memory_athlete.memoryassistant.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Scanner;

import timber.log.Timber;

import static com.memory_athlete.memoryassistant.Helper.makeCardString;
import static java.lang.Integer.parseInt;

public class RecallCards extends RecallSimple {
    CardAdapter mAdapter;
    int cardSelectorTextColour = -1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAdapter = new CardAdapter(this, responses);
        gridView.setAdapter(mAdapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                responses.remove(position);
                mAdapter.notifyDataSetChanged();
            }
        });
    }

    private void theme() {
        String theme = sharedPreferences.getString(getString(R.string.theme), "AppTheme");
        assert theme != null;
        switch (theme) {
            case "Dark":
                mSuitBackground = R.color.color_suit_background_dark;
                //gridView = findViewById(R.id.cards_responses);
                gridView.setAlpha(0.8f);
                break;
            case "Night":
                mSuitBackground = R.color.color_suit_background_night;
                //gridView = findViewById(R.id.cards_responses);
                gridView.setAlpha(0.7f);
                break;
            default:
                mSuitBackground = R.color.color_suit_background_light;
                cardSelectorTextColour = Color.argb(155, 0, 0, 0);
                //gridView = findViewById(R.id.cards_responses);
        }
    }

    void cardSelected(int card) {
        card = (card == 0 ? 12 : card - 1);
        responses.add(String.valueOf(card + selectedSuit));
        mAdapter.notifyDataSetChanged();
        gridView.smoothScrollToPosition(responses.size() - 1);
        Timber.v("cardSelected complete");
    }

    @Override
    protected void setResponseLayout(boolean onCreate) {
        if (onCreate) theme();
        gridView.setNumColumns(Integer.parseInt(Objects.requireNonNull(sharedPreferences.getString(
                getString(R.string.recall_grid_columns), "6"))));
        cardImageIds = Helper.makeCards();
        Timber.v("cardResponseLayout() started");
        compareFormat = CompareFormat.CARD_COMPARE_FORMAT;
        responseFormat = ResponseFormat.CARD_RESPONSE_FORMAT;

        LinearLayout suitLayout = findViewById(R.id.card_suit);
        if (suitLayout.getChildCount() != 0) return;
        for (int i = 0; i < 4; i++) {
            float scale = getResources().getDisplayMetrics().density;
            int dpAsPixels = (int) (8 * scale + 0.5f);

            final ImageView imageView = new ImageView(this);
            imageView.setImageResource(Helper.makeSuits()[i]);
            imageView.setContentDescription(Helper.suitDescriptions()[i]);
            imageView.setLayoutParams(new LinearLayout.LayoutParams(0,
                    LinearLayout.LayoutParams.WRAP_CONTENT, 1));
            imageView.setAdjustViewBounds(true);
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            imageView.setPadding(2 * dpAsPixels, dpAsPixels, 2 * dpAsPixels, dpAsPixels);
            imageView.setId(i);

            imageView.setOnClickListener(new View.OnClickListener() {
                @SuppressLint("ResourceType")
                @Override
                public void onClick(View view) {
                    for (int j = 0; j < 4; j++)
                        findViewById(R.id.card_suit).findViewById(j).setBackgroundColor(0);

                    view.setBackgroundColor(getResources().getColor(mSuitBackground));
                    selectedSuit = view.getId() * 13;
                    Timber.v("selectedSuit = " + selectedSuit);
                }
            });

            suitLayout.addView(imageView);
        }
        suitLayout.findViewById(0).setBackgroundColor(getResources().getColor(mSuitBackground));

        LinearLayout numberLayout = findViewById(R.id.card_numbers);
        for (int i = 0; i <= 13; i++) {
            TextView textView = new TextView(this);
            textView.setId(i);
            if (i == 0) textView.setText("A");
            else if (i < 10) textView.setText(String.valueOf(i + 1));
            else if (i == 10) textView.setText("J");
            else if (i == 11) textView.setText("Q");
            else if (i == 12) textView.setText("K");
            textView.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            float scale = getResources().getDisplayMetrics().density;
            int dpAsPixels = (int) (10 * scale + 0.5f);
            textView.setPadding(dpAsPixels, 0, dpAsPixels, 0);
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 36);
            if (cardSelectorTextColour != -1) textView.setTextColor(cardSelectorTextColour);
            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    cardSelected(view.getId());
                    Timber.v("Selected Card = " + view.getId());
                }
            });
            numberLayout.addView(textView);
        }

        findViewById(R.id.result).setVisibility(View.GONE);
        findViewById(R.id.recall_layout).setVisibility(View.GONE);
        findViewById(R.id.progress_bar_recall).setVisibility(View.GONE);
        findViewById(R.id.response_input).setVisibility(View.GONE);

        findViewById(R.id.response_layout).setVisibility(View.VISIBLE);
        findViewById(R.id.button_bar).setVisibility(View.VISIBLE);
        findViewById(R.id.card_suit).setVisibility(View.VISIBLE);
        findViewById(R.id.card_numbers).setVisibility(View.VISIBLE);
        gridView.setVisibility(View.VISIBLE);

        Timber.v("Card layout set");
    }

    @Override
    protected String formatAnswers(Scanner scanner, StringBuilder sb, String whitespace) {
        String[] cards = makeCardString();

        while (scanner.hasNext())
            sb.append(cards[Integer.parseInt(scanner.next())]).append(whitespace);

        Timber.v("giveUp() complete, returns " + sb.toString());
        return sb.toString();
    }

    @Override
    protected void getResponse() {
        //The responses were stored when they were entered so there is no need of doing anything
    }

    @Override
    protected void compare(boolean words) {
        String[] cardStrings = makeCardString();
        for (int i = 0; i < responses.size(); i++)
            responses.set(i, cardStrings[Integer.parseInt(responses.get(i))]);
        for (int i = 0; i < answers.size(); i++) {
            // TODO remove this try catch block
            try {
                answers.set(i, cardStrings[Integer.parseInt(answers.get(i))]);
            } catch (NumberFormatException e) {
                throw new NumberFormatException("The value of the string to be parsed into int i '"
                        + answers.get(i) + "'");
            }
        }
        super.compare(false);
        Timber.v("compareCards() complete");
    }

    @Override
    protected void reset() {
        super.reset();
        mAdapter.notifyDataSetChanged();
        gridView.setVisibility(View.VISIBLE);
    }

    private class CardAdapter extends ArrayAdapter<String> {

        CardAdapter(Activity context, ArrayList<String> cards) {
            super(context, 0, cards);
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            ImageView imageView = (ImageView) convertView;
            Timber.v("position " + position);
            if (convertView == null) {
                imageView = new ImageView(getApplicationContext());
                imageView.setLayoutParams(new GridView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT));
                imageView.setVisibility(View.VISIBLE);
                //imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                imageView.setAdjustViewBounds(true);
            }

            Picasso.with(getApplicationContext())
                    .load(cardImageIds[parseInt(responses.get(position))])
                    .placeholder(R.drawable.sa)
                    .fit()
                    //.centerInside()                 // or .centerCrop() to avoid a stretched imageÒ
                    .into(imageView);

            Timber.v("getView() complete");

            //((ImageView) listItemView.findViewById(R.id.card_image)).setImageResource(
            //      cardImageIds[parseInt(responses.get(position))]);
            return imageView;//listItemView;
        }
    }
}


// TODO: uses custom theme(), don't remove this comment