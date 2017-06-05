package com.ckfree.common;


import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.SpannedString;
import android.text.TextWatcher;
import android.text.style.AbsoluteSizeSpan;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;

/**
 * Created by Conan on 2017/6/4.
 */

public class SimpleSearchBar extends FrameLayout implements View.OnClickListener, TextWatcher, TextView.OnEditorActionListener, View.OnFocusChangeListener {

    private ViewHolder holder;
    private View displayView;
    private SearchBarWathcer mSearchBarWathcer;
    private Subscription mob;

    public SimpleSearchBar(@NonNull Context context) {
        super(context);
    }

    public SimpleSearchBar(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        FrameLayout searchBarView = (FrameLayout) LayoutInflater.from(context).inflate(R.layout.layout_mysearchbar, null);
        holder = new ViewHolder(searchBarView);
        holder.etSearch.addTextChangedListener(this);
        holder.etSearch.setOnEditorActionListener(this);
        holder.etSearch.setOnFocusChangeListener(this);
        holder.tvCancel.setOnClickListener(this);
        holder.fmEnter.setOnClickListener(this);
        initStyle(attrs);

        addView(searchBarView);
    }

    private void initStyle(AttributeSet attrs) {
        TypedArray t = getContext().obtainStyledAttributes(attrs, R.styleable.NewMyElement);

        String mCancelText = t.getString(R.styleable.NewMyElement_mCancelText);
        float mCancelTextSize = t.getDimension(R.styleable.NewMyElement_mCancelTextSize, 14);
        int mCancelTextColor = t.getColor(R.styleable.NewMyElement_mCancelTextColor, getResources().getColor(R.color.item_2db7b5));

        float mTextSize = t.getDimension(R.styleable.NewMyElement_mTextSize, 13);
        int mTextColor = t.getColor(R.styleable.NewMyElement_mTextColor, getResources().getColor(R.color.item_333333));

        String mHintText = t.getString(R.styleable.NewMyElement_mHintText);
        float mHintSize = t.getDimension(R.styleable.NewMyElement_mHintSize, 13);
        int mHintColor = t.getColor(R.styleable.NewMyElement_mHintColor, getResources().getColor(R.color.item_999999));

        mHintText = (mHintText == null ? getResources().getString(R.string.default_hint) : mHintText);
        holder.tvEnterHint.setText(mHintText);
        holder.tvEnterHint.setTextSize(mHintSize);
        holder.tvEnterHint.setTextColor(mHintColor);

        SpannableString hint = new SpannableString(mHintText);
        AbsoluteSizeSpan ass = new AbsoluteSizeSpan((int) mHintSize, true);
        hint.setSpan(ass, 0, hint.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        holder.etSearch.setHint(new SpannedString(hint));
        holder.etSearch.setHintTextColor(mHintColor);

        holder.etSearch.setTextSize(mTextSize);
        holder.etSearch.setTextColor(mTextColor);

        if (mCancelText != null) holder.tvCancel.setText(mCancelText);
        holder.tvCancel.setTextSize(mCancelTextSize);
        holder.tvCancel.setTextColor(mCancelTextColor);
    }

    //两个点击事件,控制输入框焦点，进而控制布局
    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.fm_enter) {
            Utils.showSoftInput(holder.etSearch);
        } else if (v.getId() == R.id.tv_cancel) {
            holder.etSearch.setText("");
            Utils.hideSoftInput(getContext(), holder.etSearch);
            holder.etSearch.clearFocus();
        }
    }

    //软键盘事件
    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_SEARCH) {
            String s = v.getText().toString();
            mSearchBarWathcer._onTextChanged(s);
        }
        return true;
    }

    //输入框文字监听
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    //↑
    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    //↑
    @Override
    public void afterTextChanged(final Editable s) {

        if (mob != null && !mob.isUnsubscribed()) mob.unsubscribe();

        if (mSearchBarWathcer != null)
            mob = Observable.timer(200, TimeUnit.MILLISECONDS)
                    .compose(RxSchedulers.<Long>io_main())
                    .subscribe(new Subscriber<Long>() {
                        @Override
                        public void onCompleted() {

                        }

                        @Override
                        public void onError(Throwable e) {

                        }

                        @Override
                        public void onNext(Long aLong) {
                            mSearchBarWathcer._onTextChanged(s.toString());
                        }
                    });
    }

    //输入框焦点监听，实现布局更换
    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        holder.fmEnter.setVisibility(hasFocus ? GONE : VISIBLE);
        holder.linSearchView.setVisibility(hasFocus ? VISIBLE : GONE);
        if (displayView != null) displayView.setVisibility(hasFocus ? VISIBLE : GONE);
    }

    static class ViewHolder {
        EditText etSearch;
        TextView tvCancel;
        LinearLayout linSearchView;
        TextView tvEnterHint;
        FrameLayout fmEnter;

        ViewHolder(View view) {
            etSearch = (EditText) view.findViewById(R.id.et_search);
            tvCancel = (TextView) view.findViewById(R.id.tv_cancel);
            linSearchView = (LinearLayout) view.findViewById(R.id.lin_searchview);
            tvEnterHint = (TextView) view.findViewById(R.id.tv_enterhint);
            fmEnter = (FrameLayout) view.findViewById(R.id.fm_enter);
        }
    }

    //*********对外接口 ↓*****************
    public abstract static class SearchBarWathcer {
        protected abstract void _onTextChanged(String s);
    }

    //设定展示View, 添加文本监听
    public void init(View displayView, SearchBarWathcer mSearchBarWathcer) {
        this.displayView = displayView;
        displayView.setVisibility(GONE);

        this.mSearchBarWathcer = mSearchBarWathcer;
    }
    //*********对外接口 ↑*****************

}
