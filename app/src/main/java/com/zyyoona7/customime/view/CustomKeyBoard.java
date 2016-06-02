package com.zyyoona7.customime.view;

import android.content.Context;
import android.os.Handler;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.googlecode.openwnn.legacy.CLOUDSONG.CandidateView;
import com.googlecode.openwnn.legacy.CLOUDSONG.CloudKeyboardInputManager;
import com.googlecode.openwnn.legacy.CLOUDSONG.OnCandidateSelected;
import com.googlecode.openwnn.legacy.CLOUDSONG.OnPinyinQueryed;
import com.googlecode.openwnn.legacy.CLOUDSONG.PinyinQueryResult;
import com.googlecode.openwnn.legacy.OnHandWritingRecognize;
import com.googlecode.openwnn.legacy.WnnWord;
import com.googlecode.openwnn.legacy.handwritingboard.HandWritingBoardLayout;
import com.zyyoona7.customime.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by User on 2016/6/1.
 */
public class CustomKeyBoard extends FrameLayout implements View.OnClickListener, OnCandidateSelected, OnHandWritingRecognize, OnPinyinQueryed, BaseQuickAdapter.OnRecyclerViewItemClickListener {
    private static final String TAG = "CustomKeyBoard";

    //拼音或英文标识
    public static final int PINYIN = 1;
    public static final int ENGLISH = 2;

    //显示字列表的view
    private CandidateView mCandidateView;

    //显示更多字按钮，回删按钮，清空按钮，擦除重写按钮，向上收起按钮
    private Button mBtnMore, mBtnDelete, mBtnClear, mBtnWipeAll, mBtnUpMore;

    //键盘26个按键加空格和数字键
    private Button mBtnQ, mBtnW, mBtnE, mBtnR, mBtnT, mBtnY, mBtnU, mBtnI, mBtnO, mBtnP, mBtnA, mBtnS, mBtnD, mBtnF, mBtnG, mBtnH, mBtnJ, mBtnK, mBtnL, mBtnZ, mBtnX, mBtnC, mBtnV, mBtnB, mBtnN, mBtnM, mBtnEnNum, mBtnEnSpace;

    //数字键盘加空格和英文按钮
    private Button mBtnOne, mBtnTwo, mBtnThree, mBtnFour, mBtnFive, mBtnSix, mBtnSeven, mBtnEight, mBtnNine, mBtnZero, mBtnEnglish, mBtnNumSpace;
    private ImageButton mBtnReturn;
    //3个radioButton
    private RadioButton mRbEnglish, mRbPinyin, mRbHandWrite;

    //英文(拼音)和数字键盘布局
    private LinearLayout mllEnglishKbLayout, mllNumKbLayout;

    //手写键盘布局
    private RelativeLayout mRlHandWriteLayout;

    //输入框
    private EditText mEditCharactor;

    //显示更多字的rv
    private RecyclerView mMoreRecyclerView;

    //显示更多字的布局
    private LinearLayout mllMoreRv;

    //显示文字布局
    private LinearLayout mllCandidateLayout;

    //手写view
    private HandWritingBoardLayout mHandWritingBoard;

    //显示拼音的textView
    private TextView mTvPinyin;

    //判断当前keyboard是英文还是拼音
    private int mCurrentKeyboard = 1;

    //当前输入的文字
    private StringBuilder currentInput = new StringBuilder();
    private CloudKeyboardInputManager ckManager;

    //adapter
    private CandidateRvAdapter mCandidateRvAdapter;

    //保存当前的word list
    private List<WnnWord> currentWordList;

    private OnEditSearchListener mOnEditSearchListener;

    //搜索做延时处理
    private Handler mDelayHandler = new Handler();

    public interface OnEditSearchListener {
        /**
         * 搜索回调
         *
         * @param text
         */
        void onSearch(String text);
    }

    public CustomKeyBoard(Context context) {
        this(context, null);
    }

    public CustomKeyBoard(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.custom_keyboard_layout, this, true);
        //初始化自定义属性
        initAttrs(attrs);
        initViews();
    }

    /**
     * 初始化自定义属性
     *
     * @param attrs
     */
    private void initAttrs(AttributeSet attrs) {
    }

    /**
     * 初始化view
     */
    private void initViews() {
        ckManager = new CloudKeyboardInputManager();
        ckManager.setOnPinyinQueryed(this);
        mCandidateView = (CandidateView) findViewById(R.id.id_candidate_view);
        mCandidateView.setOnCandidateSelected(this);
        mHandWritingBoard = (HandWritingBoardLayout) findViewById(R.id.id_hand_write_board);
        mHandWritingBoard.setOnHandWritingRecognize(this);
        mTvPinyin = (TextView) findViewById(R.id.id_tv_pinyin);
        mEditCharactor = (EditText) findViewById(R.id.id_et_input);
        mBtnMore = (Button) findViewById(R.id.id_btn_more);
        mBtnDelete = (Button) findViewById(R.id.id_btn_delete);
        mBtnClear = (Button) findViewById(R.id.id_btn_clear);
        mRbEnglish = (RadioButton) findViewById(R.id.id_rb_english);
        mRbPinyin = (RadioButton) findViewById(R.id.id_rb_pinyin);
        mRbHandWrite = (RadioButton) findViewById(R.id.id_rb_hand_write);
        mBtnWipeAll = (Button) findViewById(R.id.id_btn_wipe_all);
        mBtnUpMore = (Button) findViewById(R.id.id_btn_up_rv);
        mBtnMore.setOnClickListener(this);
        mBtnDelete.setOnClickListener(this);
        mBtnClear.setOnClickListener(this);
        mRbEnglish.setOnClickListener(this);
        mRbPinyin.setOnClickListener(this);
        mRbHandWrite.setOnClickListener(this);
        mBtnWipeAll.setOnClickListener(this);
        mBtnUpMore.setOnClickListener(this);
        //初始化英文键盘
        initEnglishKeyboard();

        //初始化数字键盘
        initNumberKeyboard();

        //初始化RecyclerView设置adapter
        mMoreRecyclerView = (RecyclerView) findViewById(R.id.id_recycler_view_more);
        mMoreRecyclerView.setHasFixedSize(true);
        mMoreRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 15, GridLayoutManager.VERTICAL, false));
        mMoreRecyclerView.addItemDecoration(new DividerGridItemDecoration(getContext(), 1, getResources().getColor(android.R.color.white)));
        mCandidateRvAdapter = new CandidateRvAdapter(getContext());
        mMoreRecyclerView.setAdapter(mCandidateRvAdapter);
        mCandidateRvAdapter.setOnRecyclerViewItemClickListener(this);

        mllEnglishKbLayout = (LinearLayout) findViewById(R.id.id_custom_keyboard);
        mllNumKbLayout = (LinearLayout) findViewById(R.id.id_custom_num);
        mRlHandWriteLayout = (RelativeLayout) findViewById(R.id.id_custom_hand_write);
        mllCandidateLayout = (LinearLayout) findViewById(R.id.id_custom_candidate_layout);
        mllMoreRv = (LinearLayout) findViewById(R.id.id_ll_rv_more);

        //初始化显示键盘
        initKeyboardState();
        //给editText设置监听器
        setTextChangeListener();
    }

    private void setTextChangeListener() {
        mEditCharactor.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(final Editable s) {
                if (mOnEditSearchListener != null && !TextUtils.isEmpty(s.toString())) {
                    //延时处理搜索
                    mDelayHandler.removeCallbacksAndMessages(null);
                    mDelayHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mOnEditSearchListener.onSearch(s.toString());
                        }
                    }, 500);
                }
            }
        });
    }

    /**
     * 初始化显示键盘
     */
    private void initKeyboardState() {
        mllEnglishKbLayout.setVisibility(VISIBLE);
        mllNumKbLayout.setVisibility(GONE);
        mRlHandWriteLayout.setVisibility(GONE);
        mRbEnglish.setChecked(true);
        mCurrentKeyboard = ENGLISH;
        //隐藏候选字布局
        hideCandidate();
    }

    /**
     * 初始化键盘key
     */
    private void initEnglishKeyboard() {
        mBtnQ = (Button) findViewById(R.id.id_btn_Q);
        mBtnQ.setOnClickListener(this);
        mBtnW = (Button) findViewById(R.id.id_btn_W);
        mBtnW.setOnClickListener(this);
        mBtnE = (Button) findViewById(R.id.id_btn_E);
        mBtnE.setOnClickListener(this);
        mBtnR = (Button) findViewById(R.id.id_btn_R);
        mBtnR.setOnClickListener(this);
        mBtnT = (Button) findViewById(R.id.id_btn_T);
        mBtnT.setOnClickListener(this);
        mBtnY = (Button) findViewById(R.id.id_btn_Y);
        mBtnY.setOnClickListener(this);
        mBtnU = (Button) findViewById(R.id.id_btn_U);
        mBtnU.setOnClickListener(this);
        mBtnI = (Button) findViewById(R.id.id_btn_I);
        mBtnI.setOnClickListener(this);
        mBtnO = (Button) findViewById(R.id.id_btn_O);
        mBtnO.setOnClickListener(this);
        mBtnP = (Button) findViewById(R.id.id_btn_P);
        mBtnP.setOnClickListener(this);
        mBtnA = (Button) findViewById(R.id.id_btn_A);
        mBtnA.setOnClickListener(this);
        mBtnS = (Button) findViewById(R.id.id_btn_S);
        mBtnS.setOnClickListener(this);
        mBtnD = (Button) findViewById(R.id.id_btn_D);
        mBtnD.setOnClickListener(this);
        mBtnF = (Button) findViewById(R.id.id_btn_F);
        mBtnF.setOnClickListener(this);
        mBtnG = (Button) findViewById(R.id.id_btn_G);
        mBtnG.setOnClickListener(this);
        mBtnH = (Button) findViewById(R.id.id_btn_H);
        mBtnH.setOnClickListener(this);
        mBtnJ = (Button) findViewById(R.id.id_btn_J);
        mBtnJ.setOnClickListener(this);
        mBtnK = (Button) findViewById(R.id.id_btn_K);
        mBtnK.setOnClickListener(this);
        mBtnL = (Button) findViewById(R.id.id_btn_L);
        mBtnL.setOnClickListener(this);
        mBtnZ = (Button) findViewById(R.id.id_btn_Z);
        mBtnZ.setOnClickListener(this);
        mBtnX = (Button) findViewById(R.id.id_btn_X);
        mBtnX.setOnClickListener(this);
        mBtnC = (Button) findViewById(R.id.id_btn_C);
        mBtnC.setOnClickListener(this);
        mBtnV = (Button) findViewById(R.id.id_btn_V);
        mBtnV.setOnClickListener(this);
        mBtnB = (Button) findViewById(R.id.id_btn_B);
        mBtnB.setOnClickListener(this);
        mBtnN = (Button) findViewById(R.id.id_btn_N);
        mBtnN.setOnClickListener(this);
        mBtnM = (Button) findViewById(R.id.id_btn_M);
        mBtnM.setOnClickListener(this);
        mBtnEnNum = (Button) findViewById(R.id.id_btn_num);
        mBtnEnNum.setOnClickListener(this);
        mBtnEnSpace = (Button) findViewById(R.id.id_btn_space);
        mBtnEnSpace.setOnClickListener(this);
    }

    /**
     * 初始化数字键盘
     */
    private void initNumberKeyboard() {
        mBtnOne = (Button) findViewById(R.id.id_btn_one);
        mBtnOne.setOnClickListener(this);
        mBtnTwo = (Button) findViewById(R.id.id_btn_two);
        mBtnTwo.setOnClickListener(this);
        mBtnThree = (Button) findViewById(R.id.id_btn_three);
        mBtnThree.setOnClickListener(this);
        mBtnFour = (Button) findViewById(R.id.id_btn_four);
        mBtnFour.setOnClickListener(this);
        mBtnFive = (Button) findViewById(R.id.id_btn_five);
        mBtnFive.setOnClickListener(this);
        mBtnSix = (Button) findViewById(R.id.id_btn_six);
        mBtnSix.setOnClickListener(this);
        mBtnSeven = (Button) findViewById(R.id.id_btn_seven);
        mBtnSeven.setOnClickListener(this);
        mBtnEight = (Button) findViewById(R.id.id_btn_eight);
        mBtnEight.setOnClickListener(this);
        mBtnNine = (Button) findViewById(R.id.id_btn_nine);
        mBtnNine.setOnClickListener(this);
        mBtnZero = (Button) findViewById(R.id.id_btn_zero);
        mBtnZero.setOnClickListener(this);
        mBtnEnglish = (Button) findViewById(R.id.id_btn_english);
        mBtnEnglish.setOnClickListener(this);
        mBtnNumSpace = (Button) findViewById(R.id.id_btn_num_space);
        mBtnNumSpace.setOnClickListener(this);
        mBtnReturn = (ImageButton) findViewById(R.id.id_btn_return);
        mBtnReturn.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.id_btn_more:
                //显示更多按钮
                showMoreRv();
                break;
            case R.id.id_btn_delete:
                //回删按钮
                delCurrentInput();
                resetHandWritingRecognizeClicked();
                break;
            case R.id.id_btn_clear:
                //清空
                resetHandWritingRecognizeClicked();
                clearCurrentInput();
                cleanCurrentPinyinView();
                ckManager.delAll();
                mEditCharactor.setText("");
                //隐藏候选者布局
                hideCandidate();
                break;
            case R.id.id_btn_wipe_all:
                //擦除重写
                //隐藏候选者布局
                hideCandidate();
                resetHandWritingRecognizeClicked();
                break;
            case R.id.id_btn_up_rv:
                //向上收起更多
                showCandidate();
                break;
            case R.id.id_rb_english:
                //rb英文
                mCurrentKeyboard = ENGLISH;
                mBtnMore.setVisibility(VISIBLE);
                showEnglishKeyboard();
                //中英文切换的时候拼音view有内容则放入editText中
                if (!TextUtils.isEmpty(mTvPinyin.getText().toString())) {
                    appendCandidate(mTvPinyin.getText().toString());
                    mEditCharactor.setText(currentInput.toString());
                }
                break;
            case R.id.id_rb_pinyin:
                //rb拼音
                mCurrentKeyboard = PINYIN;
                mBtnMore.setVisibility(VISIBLE);
                showEnglishKeyboard();
                break;
            case R.id.id_rb_hand_write:
                //rb手写
                //手写时隐藏更多键(手写时没有那么多字)
                mBtnMore.setVisibility(GONE);
                showHandWriteKeyBoard();
                break;
            case R.id.id_btn_Q:
                //Q
                processInput(new char[]{'q'});
                break;
            case R.id.id_btn_W:
                //W
                processInput(new char[]{'w'});
                break;
            case R.id.id_btn_E:
                //E
                processInput(new char[]{'e'});
                break;
            case R.id.id_btn_R:
                //R
                processInput(new char[]{'r'});
                break;
            case R.id.id_btn_T:
                //T
                processInput(new char[]{'t'});
                break;
            case R.id.id_btn_Y:
                //Y
                processInput(new char[]{'y'});
                break;
            case R.id.id_btn_U:
                //U
                processInput(new char[]{'u'});
                break;
            case R.id.id_btn_I:
                //I
                processInput(new char[]{'i'});
                break;
            case R.id.id_btn_O:
                //O
                processInput(new char[]{'o'});
                break;
            case R.id.id_btn_P:
                //P
                processInput(new char[]{'p'});
                break;
            case R.id.id_btn_A:
                //A
                processInput(new char[]{'a'});
                break;
            case R.id.id_btn_S:
                //S
                processInput(new char[]{'s'});
                break;
            case R.id.id_btn_D:
                //D
                processInput(new char[]{'d'});
                break;
            case R.id.id_btn_F:
                //F
                processInput(new char[]{'f'});
                break;
            case R.id.id_btn_G:
                //G
                processInput(new char[]{'g'});
                break;
            case R.id.id_btn_H:
                //H
                processInput(new char[]{'h'});
                break;
            case R.id.id_btn_J:
                //J
                processInput(new char[]{'j'});
                break;
            case R.id.id_btn_K:
                //K
                processInput(new char[]{'k'});
                break;
            case R.id.id_btn_L:
                //L
                processInput(new char[]{'l'});
                break;
            case R.id.id_btn_Z:
                //Z
                processInput(new char[]{'z'});
                break;
            case R.id.id_btn_X:
                //X
                processInput(new char[]{'x'});
                break;
            case R.id.id_btn_C:
                //C
                processInput(new char[]{'c'});
                break;
            case R.id.id_btn_V:
                //V
                processInput(new char[]{'v'});
                break;
            case R.id.id_btn_B:
                //B
                processInput(new char[]{'b'});
                break;
            case R.id.id_btn_N:
                //N
                processInput(new char[]{'n'});
                break;
            case R.id.id_btn_M:
                //M
                processInput(new char[]{'m'});
                break;
            case R.id.id_btn_num:
                //english keyboard num
                showNumberKeyboard();
                break;
            case R.id.id_btn_space:
                //english keyboard space
                handleSpace();
                break;
            case R.id.id_btn_one:
                //one
                processInputNumber("1");
                break;
            case R.id.id_btn_two:
                //two
                processInputNumber("2");
                break;
            case R.id.id_btn_three:
                //three
                processInputNumber("3");
                break;
            case R.id.id_btn_four:
                //four
                processInputNumber("4");
                break;
            case R.id.id_btn_five:
                //five
                processInputNumber("5");
                break;
            case R.id.id_btn_six:
                //six
                processInputNumber("6");
                break;
            case R.id.id_btn_seven:
                //seven
                processInputNumber("7");
                break;
            case R.id.id_btn_eight:
                //eight
                processInputNumber("8");
                break;
            case R.id.id_btn_nine:
                //nine
                processInputNumber("9");
                break;
            case R.id.id_btn_zero:
                //zero
                processInputNumber("0");
                break;
            case R.id.id_btn_english:
                //number keyboard english key
                showEnglishKeyboard();
                break;
            case R.id.id_btn_num_space:
                //number keyboard space key
                handleSpace();
                break;
            case R.id.id_btn_return:
                //number keyboard return key
                showEnglishKeyboard();
                break;
        }
    }

    /**
     * 处理空格事件
     * 如果候选字集合不为空选中第一个字，否则追加空格
     */
    private void handleSpace() {
        List<WnnWord> suggestions = mCandidateView.getSuggestions();
        if (suggestions.size() >= 1) {
            handleCandidateSelected(suggestions.get(0));
            hideCandidate();
        } else {
            ckManager.delAll();
            appendCandidate(" ");
            mEditCharactor.setText(currentInput.toString());
        }
    }

    /**
     * 是否是手写模式
     *
     * @return
     */
    private boolean isHandWriting() {
        return mHandWritingBoard.isShown();
    }

    /**
     * 显示英文键盘
     */
    private void showEnglishKeyboard() {
        mllEnglishKbLayout.setVisibility(VISIBLE);
        mllNumKbLayout.setVisibility(GONE);
        mRlHandWriteLayout.setVisibility(GONE);
        ckManager.delAll();
        resetHandWritingRecognize();
        mCandidateView.clear();
        //隐藏候选字布局
        hideCandidate();
    }

    /**
     * 显示数字键盘
     */
    private void showNumberKeyboard() {
        resetHandWritingRecognize();
        mCandidateView.clear();
        mllEnglishKbLayout.setVisibility(GONE);
        mllNumKbLayout.setVisibility(VISIBLE);
        mRlHandWriteLayout.setVisibility(GONE);
        if (mCurrentKeyboard == PINYIN) {
            mBtnEnglish.setText("拼音");
        } else {
            mBtnEnglish.setText("英文");
        }
        //隐藏候选字布局
        hideCandidate();
    }

    /**
     * 显示手写键盘
     */
    private void showHandWriteKeyBoard() {
        mllEnglishKbLayout.setVisibility(GONE);
        mllNumKbLayout.setVisibility(GONE);
        mRlHandWriteLayout.setVisibility(VISIBLE);
        ckManager.delAll();
        //清空显示拼音的view
        mTvPinyin.setText("");
        mCandidateView.clear();
        resetHandWritingRecognize();
        //隐藏候选字布局
        hideCandidate();
    }

    /**
     * 显示更多字的Rv
     */
    private void showMoreRv() {
        mllMoreRv.setVisibility(VISIBLE);
        mllCandidateLayout.setVisibility(INVISIBLE);
        mMoreRecyclerView.scrollToPosition(0);
    }

    /**
     * 显示字的candidate
     */
    private void showCandidate() {
        mllMoreRv.setVisibility(GONE);
        mllCandidateLayout.setVisibility(VISIBLE);
    }

    /**
     * 隐藏candidateView
     */
    private void hideCandidate() {
        mllMoreRv.setVisibility(GONE);
        mllCandidateLayout.setVisibility(INVISIBLE);
    }


    /**
     * 处理输入
     *
     * @param chars
     */
    private void processInput(char[] chars) {
        switch (mCurrentKeyboard) {
            case PINYIN:
                if (!mllCandidateLayout.isShown()) {
                    //显示候选字
                    showCandidate();
                }
                ckManager.processInput(chars);
                break;
            case ENGLISH:
                if (chars != null && chars.length > 0) {
                    appendCandidate(chars[0] + "");
                    mEditCharactor.setText(currentInput.toString());
                }
                break;
        }
    }

    /**
     * 处理输入数字
     *
     * @param num
     */
    private void processInputNumber(String num) {
        appendCandidate(num);
        mEditCharactor.setText(currentInput.toString());
    }

    /**
     * 清空currentInput的内容
     */
    private void clearCurrentInput() {
        currentInput.delete(0, currentInput.length());
    }

    /**
     * 回删操作
     */
    private void delCurrentInput() {
        if (isHandWriting()) {
            delCurrentInputNoPinyin();
        } else {
            if (mTvPinyin.getText().length() > 0) {
                ckManager.processDel();
            } else {
                mCandidateView.clear();
                //隐藏候选者布局
                hideCandidate();
                delCurrentInputNoPinyin();
            }
        }
    }

    /**
     * 删除没有拼音的情况
     */
    private void delCurrentInputNoPinyin() {
        if (currentInput.length() > 0) {
            currentInput.deleteCharAt(currentInput.length() - 1);
        }
        mEditCharactor.setText(currentInput.toString());
    }

    @Override
    public void candidateSelected(WnnWord wnnWord) {
        handleCandidateSelected(wnnWord);
        //隐藏候选者布局
        hideCandidate();
    }

    /**
     * 处理candidate选择事件
     *
     * @param wnnWord
     */
    private void handleCandidateSelected(WnnWord wnnWord) {
        String candidate = null;
        if (wnnWord != null) {
            candidate = wnnWord.candidate;
        }
        if (!TextUtils.isEmpty(candidate)) {
            cleanCurrentPinyinView();
            appendCandidate(candidate);
            mEditCharactor.setText(currentInput.toString());
        }
        // mOpenWnnZHCN.commitTextSelected(wnnWord);
        mCandidateView.clear();
        if (isHandWriting()) {
            resetHandWritingRecognize();
        } else {
            ckManager.candidateSelected(wnnWord);
        }
    }

    /**
     * 清空显示的拼音
     */
    private void cleanCurrentPinyinView() {
        mTvPinyin.setText("");
    }

    /**
     * 向后追加文字
     *
     * @param candidate
     */
    private void appendCandidate(String candidate) {
        currentInput.append(candidate);
        // currentIndex += candidate.length();
    }

    private void resetHandWritingRecognizeClicked() {
        resetHandWritingRecognize();
        mCandidateView.clear();
    }

    /**
     * 清空手写内容
     */
    private void resetHandWritingRecognize() {
        mHandWritingBoard.reset_recognize();
    }

    @Override
    public void handWritingRecognized(ArrayList<WnnWord> result) {
        if (!mllCandidateLayout.isShown()) {
            //显示候选字
            showCandidate();
        }
//        currentWordList=result;
        mCandidateView.setSuggestions(result, false, false);
//        mCandidateRvAdapter.setNewData(result);
    }

    @Override
    public void onPinyinQueryed(PinyinQueryResult pyQueryResult) {
        if (pyQueryResult != null) {
            currentWordList = pyQueryResult.getCandidateList();
            mCandidateView.setSuggestions(pyQueryResult.getCandidateList(), false, false);
            mCandidateRvAdapter.setNewData(pyQueryResult.getCandidateList());
            String pinyin = pyQueryResult.getCurrentInput();
            updatePinyin(pinyin);
            if (TextUtils.isEmpty(pinyin)) {
                hideCandidate();
            }
        }
    }

    private void updatePinyin(String pinyin) {
//        更新拼音view
        mTvPinyin.setText(pinyin);
    }


    @Override
    public void onItemClick(View view, int i) {
        if (currentWordList != null && i < currentWordList.size()) {
            handleCandidateSelected(currentWordList.get(i));
            hideCandidate();
        }
    }

    /**
     * 设置监听器
     *
     * @param listener
     */
    public void setOnEditSearchListener(OnEditSearchListener listener) {
        this.mOnEditSearchListener = listener;
    }

    @Override
    protected void onDetachedFromWindow() {
        mDelayHandler.removeCallbacksAndMessages(null);
        super.onDetachedFromWindow();
    }
}
