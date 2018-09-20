package me.mazhiwei.library;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.NestedScrollingParent;
import android.support.v4.view.NestedScrollingParentHelper;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Scroller;

/**
 * Author: mazhiwei
 * Date: 18/9/12
 * E-mail: mazhiwei1004@gmail.com
 */
public class InstaScrollView extends FrameLayout implements NestedScrollingParent {
    private static final String TAG = InstaScrollView.class.getSimpleName();

    private static final int HEAD_OFFSET = 150;
    private boolean isTouchDivide = false;
    private boolean shouldMoveHead = false;
    private int mLastY;
    private NestedScrollingParentHelper mNestedScrollingParentHelper;
    private Scroller mScroller;
    private boolean isExpand = true;

    // 用于停止滑动时自动滑动方向
    private static final int SCROLL_IDLE = 0;
    private static final int SCROLL_UP = 1;
    private static final int SCROLL_DOWN = 2;
    private int autoScrollDir = SCROLL_IDLE;

    public InstaScrollView(@NonNull Context context) {
        this(context, null);
    }

    public InstaScrollView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public InstaScrollView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void expandHead() {
        if (!isExpand) {
            mScroller.startScroll(0, getDivideY(), 0, getHeadView().getHeight() - getDivideY());
            invalidate();
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        int childCount = getChildCount();
        if (childCount != 2) {
            throw new IllegalArgumentException(TAG + " must have two child view.");
        }
        getRecyclerView().addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (isRecyclerViewToBottom()) {
                    mScroller.startScroll(0, getDivideY(), 0, getHeadView().getHeight() - getDivideY());
                    invalidate();
                }
            }
        });
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        getRecyclerView().measure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(height - HEAD_OFFSET, MeasureSpec.EXACTLY));
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (isExpand) {
            getHeadView().layout(left, top, right, getHeadView().getMeasuredHeight());
            getRecyclerView().setPadding(0, 0, 0, getHeadView().getMeasuredHeight() - HEAD_OFFSET);
            getRecyclerView().layout(left, getHeadView().getMeasuredHeight(),
                    right, getHeadView().getMeasuredHeight() + getRecyclerView().getMeasuredHeight());
        } else {
            getHeadView().layout(left, HEAD_OFFSET - getHeadView().getMeasuredHeight(), right, HEAD_OFFSET);
            getRecyclerView().layout(left, HEAD_OFFSET,
                    right, HEAD_OFFSET + getRecyclerView().getMeasuredHeight());
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        int action = event.getAction();
        int touchY = (int) event.getY();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mLastY = touchY;
                if (Math.abs(touchY - getDivideY()) < 20) {
                    isTouchDivide = true;
                    return true;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (isTouchDivide) {
                    int offsetY = touchY - mLastY;
                    if (offsetY < 0) {
                        int totalOffset = HEAD_OFFSET - getDivideY();

                        offsetY = offsetY < totalOffset ? totalOffset : offsetY;

                        getHeadView().offsetTopAndBottom(offsetY);
                        if (getRecyclerView().getBottom() - getHeight() > 0) {
                            getRecyclerView().offsetTopAndBottom(offsetY);
                        }
                    }
                } else {
                    // in moving
                    if (!shouldMoveHead && mLastY > getDivideY() && touchY <= getDivideY()) {
                        shouldMoveHead = true;
                    }
                }
                mLastY = touchY;
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (isTouchDivide) {
                    if (isRecyclerViewToBottom()) {
                        // 列表滑到底部，直接弹出 HeadView
                        if (getDivideY() != getHeadView().getHeight()) {
                            mScroller.startScroll(0, getDivideY(), 0, getHeadView().getHeight() - getDivideY());
                            invalidate();
                        }
                    } else {
                        if (getDivideY() < getCollapseYCollapse()) {
                            mScroller.startScroll(0, getDivideY(), 0, HEAD_OFFSET - getDivideY());
                            invalidate();
                        } else {
                            mScroller.startScroll(0, getDivideY(), 0, getHeadView().getHeight() - getDivideY());
                            invalidate();
                        }
                    }
                }
                shouldMoveHead = false;
                isTouchDivide = false;
                break;
        }
        return super.dispatchTouchEvent(event);
    }

    @Override
    public boolean onStartNestedScroll(@NonNull View child, @NonNull View target, int nestedScrollAxes) {
        return isEnabled() && (nestedScrollAxes & ViewCompat.SCROLL_AXIS_VERTICAL) != 0;
    }

    @Override
    public void onNestedScrollAccepted(@NonNull View child, @NonNull View target, int axes) {
        mNestedScrollingParentHelper.onNestedScrollAccepted(child, target, axes);
    }

    @Override
    public void onNestedPreScroll(@NonNull View target, int dx, int dy, @NonNull int[] consumed) {
        if (dy < 0) {
            // 往下滑
        } else if (dy > 0) {
            // 往上滑
        }

        if (shouldMoveHead) {
            int totalOffsetY = getDivideY() - HEAD_OFFSET;
            int offsetY = dy > totalOffsetY ? totalOffsetY : dy;
            consumed[1] = offsetY;
            getHeadView().offsetTopAndBottom(-offsetY);
            if (getRecyclerView().getBottom() - getHeight() > 0) {
                getRecyclerView().offsetTopAndBottom(-offsetY);
            }
            autoScrollDir = SCROLL_UP;
        }
    }

    @Override
    public void onNestedScroll(View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
        if (dyUnconsumed < 0 && isRecyclerViewToTop()) {
            // 列表下拉到顶部时，拉出 HeadView
            int dy = -dyUnconsumed;
            int totalOffset = getHeadView().getHeight() - getDivideY();

            int offset = dy > totalOffset ? totalOffset : dy;
            getHeadView().offsetTopAndBottom(offset);
            getRecyclerView().offsetTopAndBottom(offset);
            autoScrollDir = SCROLL_DOWN;
        }
    }

    @Override
    public int getNestedScrollAxes() {
        return mNestedScrollingParentHelper.getNestedScrollAxes();
    }

    @Override
    public void onStopNestedScroll(View child) {
        mNestedScrollingParentHelper.onStopNestedScroll(child);

        if (isRecyclerViewToBottom()) {
            // 列表滑到底部，直接弹出 HeadView
            mScroller.startScroll(0, getDivideY(), 0, getHeadView().getHeight() - getDivideY());
            invalidate();
            return;
        }

        if (!isHeadIdle()) {
            int threshold = 0;
            if (autoScrollDir == SCROLL_DOWN) {
                threshold = getExplandYThreshold();
            } else if (autoScrollDir == SCROLL_UP) {
                threshold = getCollapseYCollapse();
            }
            if (getDivideY() < threshold) {
                mScroller.startScroll(0, getDivideY(), 0, HEAD_OFFSET - getDivideY());
                invalidate();
            } else {
                mScroller.startScroll(0, getDivideY(), 0, getHeadView().getHeight() - getDivideY());
                invalidate();
            }
        }
        autoScrollDir = SCROLL_IDLE;
    }

    @Override
    public boolean onNestedPreFling(View target, float velocityX, float velocityY) {
        return false;
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            int offsetY = mScroller.getCurrY();
            moveDivideTo(offsetY);
            invalidate();
        }
    }

    private void init() {
        mNestedScrollingParentHelper = new NestedScrollingParentHelper(this);
        mScroller = new Scroller(getContext());
        mScroller.setFriction(0.98f);
    }

    private int getDivideY() {
        return getHeadView().getBottom();
    }

    /**
     * 获取展开Y阈值
     *
     * @return
     */
    private int getExplandYThreshold() {
        return HEAD_OFFSET + 100;
    }

    /**
     * 获取收缩Y阈值
     *
     * @return
     */
    private int getCollapseYCollapse() {
        return getHeadView().getHeight() - 100;
    }

    private boolean isHeadIdle() {
        return getDivideY() == getHeadView().getHeight() || getDivideY() == HEAD_OFFSET;
    }

    /**
     * 列表是否滑到顶部
     *
     * @return
     */
    private boolean isRecyclerViewToTop() {
        return !getRecyclerView().canScrollVertically(-1);
    }

    /**
     * 列表是否滑到底部
     *
     * @return
     */
    private boolean isRecyclerViewToBottom() {
        GridLayoutManager layoutManager = (GridLayoutManager) getRecyclerView().getLayoutManager();
        int visibleItemCount = layoutManager.getChildCount();
        int totalItemCount = layoutManager.getItemCount();
        int pastVisibleItems = layoutManager.findFirstVisibleItemPosition();
        return pastVisibleItems + visibleItemCount >= totalItemCount;
    }

    private View getHeadView() {
        return getChildAt(1);
    }

    private RecyclerView getRecyclerView() {
        return (RecyclerView) getChildAt(0);
    }

    private void moveDivideTo(int targetY) {
        isExpand = !(targetY == HEAD_OFFSET);
        getHeadView().offsetTopAndBottom(targetY - getDivideY());
        getRecyclerView().offsetTopAndBottom(targetY - getRecyclerView().getTop());
    }
}
