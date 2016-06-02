package com.zyyoona7.customime.view;

import android.content.Context;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.googlecode.openwnn.legacy.WnnWord;
import com.zyyoona7.customime.R;

/**
 * Created by User on 2016/6/2.
 */
public class CandidateRvAdapter extends BaseQuickAdapter<WnnWord> {

    public CandidateRvAdapter(Context context) {
        super(context, R.layout.custom_rv_item_layout, null);
    }

    @Override
    protected void convert(BaseViewHolder baseViewHolder, WnnWord wnnWord) {
        baseViewHolder.setText(R.id.id_tv_rv_candidate, wnnWord.candidate);
    }
}
