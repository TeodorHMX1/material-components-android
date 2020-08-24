

package com.zeoflow.material.elements.bottomnavigation;

import com.google.android.material.R;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import androidx.annotation.Dimension;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.annotation.StyleRes;
import androidx.annotation.VisibleForTesting;
import androidx.core.util.Pools;
import androidx.core.view.ViewCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat.CollectionInfoCompat;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.view.menu.MenuItemImpl;
import androidx.appcompat.view.menu.MenuView;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityNodeInfo;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;
import androidx.transition.AutoTransition;
import androidx.transition.TransitionManager;
import androidx.transition.TransitionSet;
import com.zeoflow.material.elements.badge.BadgeDrawable;
import com.zeoflow.material.elements.internal.TextScale;
import java.util.HashSet;


@RestrictTo(LIBRARY_GROUP)
public class BottomNavigationMenuView extends ViewGroup implements MenuView {
  private static final long ACTIVE_ANIMATION_DURATION_MS = 115L;
  private static final int ITEM_POOL_SIZE = 5;

  private static final int[] CHECKED_STATE_SET = {android.R.attr.state_checked};
  private static final int[] DISABLED_STATE_SET = {-android.R.attr.state_enabled};

  @NonNull private final TransitionSet set;
  private final int inactiveItemMaxWidth;
  private final int inactiveItemMinWidth;
  private final int activeItemMaxWidth;
  private final int activeItemMinWidth;
  private final int itemHeight;
  @NonNull private final OnClickListener onClickListener;
  private final Pools.Pool<BottomNavigationItemView> itemPool =
      new Pools.SynchronizedPool<>(ITEM_POOL_SIZE);

  private boolean itemHorizontalTranslationEnabled;
  @LabelVisibilityMode private int labelVisibilityMode;

  @Nullable private BottomNavigationItemView[] buttons;
  private int selectedItemId = 0;
  private int selectedItemPosition = 0;

  private ColorStateList itemIconTint;
  @Dimension private int itemIconSize;
  private ColorStateList itemTextColorFromUser;
  @Nullable private final ColorStateList itemTextColorDefault;
  @StyleRes private int itemTextAppearanceInactive;
  @StyleRes private int itemTextAppearanceActive;
  private Drawable itemBackground;
  private int itemBackgroundRes;
  private int[] tempChildWidths;
  @NonNull private SparseArray<BadgeDrawable> badgeDrawables = new SparseArray<>(ITEM_POOL_SIZE);

  private BottomNavigationPresenter presenter;
  private MenuBuilder menu;

  public BottomNavigationMenuView(Context context) {
    this(context, null);
  }

  public BottomNavigationMenuView(Context context, AttributeSet attrs) {
    super(context, attrs);
    final Resources res = getResources();
    inactiveItemMaxWidth =
        res.getDimensionPixelSize(R.dimen.design_bottom_navigation_item_max_width);
    inactiveItemMinWidth =
        res.getDimensionPixelSize(R.dimen.design_bottom_navigation_item_min_width);
    activeItemMaxWidth =
        res.getDimensionPixelSize(R.dimen.design_bottom_navigation_active_item_max_width);
    activeItemMinWidth =
        res.getDimensionPixelSize(R.dimen.design_bottom_navigation_active_item_min_width);
    itemHeight = res.getDimensionPixelSize(R.dimen.design_bottom_navigation_height);
    itemTextColorDefault = createDefaultColorStateList(android.R.attr.textColorSecondary);

    set = new AutoTransition();
    set.setOrdering(TransitionSet.ORDERING_TOGETHER);
    set.setDuration(ACTIVE_ANIMATION_DURATION_MS);
    set.setInterpolator(new FastOutSlowInInterpolator());
    set.addTransition(new TextScale());

    onClickListener =
        new OnClickListener() {
          @Override
          public void onClick(View v) {
            final BottomNavigationItemView itemView = (BottomNavigationItemView) v;
            MenuItem item = itemView.getItemData();
            if (!menu.performItemAction(item, presenter, 0)) {
              item.setChecked(true);
            }
          }
        };
    tempChildWidths = new int[BottomNavigationMenu.MAX_ITEM_COUNT];

    ViewCompat.setImportantForAccessibility(this, ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_YES);
  }

  @Override
  public void initialize(MenuBuilder menu) {
    this.menu = menu;
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    final int width = MeasureSpec.getSize(widthMeasureSpec);
    
    final int visibleCount = menu.getVisibleItems().size();
    
    final int totalCount = getChildCount();

    final int heightSpec = MeasureSpec.makeMeasureSpec(itemHeight, MeasureSpec.EXACTLY);

    if (isShifting(labelVisibilityMode, visibleCount) && itemHorizontalTranslationEnabled) {
      final View activeChild = getChildAt(selectedItemPosition);
      int activeItemWidth = activeItemMinWidth;
      if (activeChild.getVisibility() != View.GONE) {
        
        
        activeChild.measure(
            MeasureSpec.makeMeasureSpec(activeItemMaxWidth, MeasureSpec.AT_MOST), heightSpec);
        activeItemWidth = Math.max(activeItemWidth, activeChild.getMeasuredWidth());
      }
      final int inactiveCount = visibleCount - (activeChild.getVisibility() != View.GONE ? 1 : 0);
      final int activeMaxAvailable = width - inactiveCount * inactiveItemMinWidth;
      final int activeWidth =
          Math.min(activeMaxAvailable, Math.min(activeItemWidth, activeItemMaxWidth));
      final int inactiveMaxAvailable =
          (width - activeWidth) / (inactiveCount == 0 ? 1 : inactiveCount);
      final int inactiveWidth = Math.min(inactiveMaxAvailable, inactiveItemMaxWidth);
      int extra = width - activeWidth - inactiveWidth * inactiveCount;

      for (int i = 0; i < totalCount; i++) {
        if (getChildAt(i).getVisibility() != View.GONE) {
          tempChildWidths[i] = (i == selectedItemPosition) ? activeWidth : inactiveWidth;
          
          
          
          if (extra > 0) {
            tempChildWidths[i]++;
            extra--;
          }
        } else {
          tempChildWidths[i] = 0;
        }
      }
    } else {
      final int maxAvailable = width / (visibleCount == 0 ? 1 : visibleCount);
      final int childWidth = Math.min(maxAvailable, activeItemMaxWidth);
      int extra = width - childWidth * visibleCount;
      for (int i = 0; i < totalCount; i++) {
        if (getChildAt(i).getVisibility() != View.GONE) {
          tempChildWidths[i] = childWidth;
          if (extra > 0) {
            tempChildWidths[i]++;
            extra--;
          }
        } else {
          tempChildWidths[i] = 0;
        }
      }
    }

    int totalWidth = 0;
    for (int i = 0; i < totalCount; i++) {
      final View child = getChildAt(i);
      if (child.getVisibility() == GONE) {
        continue;
      }
      child.measure(
          MeasureSpec.makeMeasureSpec(tempChildWidths[i], MeasureSpec.EXACTLY), heightSpec);
      ViewGroup.LayoutParams params = child.getLayoutParams();
      params.width = child.getMeasuredWidth();
      totalWidth += child.getMeasuredWidth();
    }
    setMeasuredDimension(
        View.resolveSizeAndState(
            totalWidth, MeasureSpec.makeMeasureSpec(totalWidth, MeasureSpec.EXACTLY), 0),
        View.resolveSizeAndState(itemHeight, heightSpec, 0));
  }

  @Override
  protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
    final int count = getChildCount();
    final int width = right - left;
    final int height = bottom - top;
    int used = 0;
    for (int i = 0; i < count; i++) {
      final View child = getChildAt(i);
      if (child.getVisibility() == GONE) {
        continue;
      }
      if (ViewCompat.getLayoutDirection(this) == ViewCompat.LAYOUT_DIRECTION_RTL) {
        child.layout(width - used - child.getMeasuredWidth(), 0, width - used, height);
      } else {
        child.layout(used, 0, child.getMeasuredWidth() + used, height);
      }
      used += child.getMeasuredWidth();
    }
  }

  @Override
  public int getWindowAnimations() {
    return 0;
  }

  @Override
  public void onInitializeAccessibilityNodeInfo(@NonNull AccessibilityNodeInfo info) {
    super.onInitializeAccessibilityNodeInfo(info);
    AccessibilityNodeInfoCompat infoCompat = AccessibilityNodeInfoCompat.wrap(info);
    infoCompat.setCollectionInfo(
        CollectionInfoCompat.obtain(
             1,
             menu.getVisibleItems().size(),
             false,
             CollectionInfoCompat.SELECTION_MODE_SINGLE));
  }

  
  public void setIconTintList(ColorStateList tint) {
    itemIconTint = tint;
    if (buttons != null) {
      for (BottomNavigationItemView item : buttons) {
        item.setIconTintList(tint);
      }
    }
  }

  
  @Nullable
  public ColorStateList getIconTintList() {
    return itemIconTint;
  }

  
  public void setItemIconSize(@Dimension int iconSize) {
    this.itemIconSize = iconSize;
    if (buttons != null) {
      for (BottomNavigationItemView item : buttons) {
        item.setIconSize(iconSize);
      }
    }
  }

  
  @Dimension
  public int getItemIconSize() {
    return itemIconSize;
  }

  
  public void setItemTextColor(ColorStateList color) {
    itemTextColorFromUser = color;
    if (buttons != null) {
      for (BottomNavigationItemView item : buttons) {
        item.setTextColor(color);
      }
    }
  }

  
  public ColorStateList getItemTextColor() {
    return itemTextColorFromUser;
  }

  
  public void setItemTextAppearanceInactive(@StyleRes int textAppearanceRes) {
    this.itemTextAppearanceInactive = textAppearanceRes;
    if (buttons != null) {
      for (BottomNavigationItemView item : buttons) {
        item.setTextAppearanceInactive(textAppearanceRes);
        
        
        if (itemTextColorFromUser != null) {
          item.setTextColor(itemTextColorFromUser);
        }
      }
    }
  }

  
  @StyleRes
  public int getItemTextAppearanceInactive() {
    return itemTextAppearanceInactive;
  }

  
  public void setItemTextAppearanceActive(@StyleRes int textAppearanceRes) {
    this.itemTextAppearanceActive = textAppearanceRes;
    if (buttons != null) {
      for (BottomNavigationItemView item : buttons) {
        item.setTextAppearanceActive(textAppearanceRes);
        
        
        if (itemTextColorFromUser != null) {
          item.setTextColor(itemTextColorFromUser);
        }
      }
    }
  }

  
  @StyleRes
  public int getItemTextAppearanceActive() {
    return itemTextAppearanceActive;
  }

  
  public void setItemBackgroundRes(int background) {
    itemBackgroundRes = background;
    if (buttons != null) {
      for (BottomNavigationItemView item : buttons) {
        item.setItemBackground(background);
      }
    }
  }

  
  @Deprecated
  public int getItemBackgroundRes() {
    return itemBackgroundRes;
  }

  
  public void setItemBackground(@Nullable Drawable background) {
    itemBackground = background;
    if (buttons != null) {
      for (BottomNavigationItemView item : buttons) {
        item.setItemBackground(background);
      }
    }
  }

  
  @Nullable
  public Drawable getItemBackground() {
    if (buttons != null && buttons.length > 0) {
      
      
      return buttons[0].getBackground();
    } else {
      return itemBackground;
    }
  }

  
  public void setLabelVisibilityMode(@LabelVisibilityMode int labelVisibilityMode) {
    this.labelVisibilityMode = labelVisibilityMode;
  }

  
  public int getLabelVisibilityMode() {
    return labelVisibilityMode;
  }

  
  public void setItemHorizontalTranslationEnabled(boolean itemHorizontalTranslationEnabled) {
    this.itemHorizontalTranslationEnabled = itemHorizontalTranslationEnabled;
  }

  
  public boolean isItemHorizontalTranslationEnabled() {
    return itemHorizontalTranslationEnabled;
  }

  @Nullable
  public ColorStateList createDefaultColorStateList(int baseColorThemeAttr) {
    final TypedValue value = new TypedValue();
    if (!getContext().getTheme().resolveAttribute(baseColorThemeAttr, value, true)) {
      return null;
    }
    ColorStateList baseColor = AppCompatResources.getColorStateList(getContext(), value.resourceId);
    if (!getContext()
        .getTheme()
        .resolveAttribute(androidx.appcompat.R.attr.colorPrimary, value, true)) {
      return null;
    }
    int colorPrimary = value.data;
    int defaultColor = baseColor.getDefaultColor();
    return new ColorStateList(
        new int[][] {DISABLED_STATE_SET, CHECKED_STATE_SET, EMPTY_STATE_SET},
        new int[] {
          baseColor.getColorForState(DISABLED_STATE_SET, defaultColor), colorPrimary, defaultColor
        });
  }

  public void setPresenter(BottomNavigationPresenter presenter) {
    this.presenter = presenter;
  }

  public void buildMenuView() {
    removeAllViews();
    if (buttons != null) {
      for (BottomNavigationItemView item : buttons) {
        if (item != null) {
          itemPool.release(item);
          item.removeBadge();
        }
      }
    }

    if (menu.size() == 0) {
      selectedItemId = 0;
      selectedItemPosition = 0;
      buttons = null;
      return;
    }
    removeUnusedBadges();

    buttons = new BottomNavigationItemView[menu.size()];
    boolean shifting = isShifting(labelVisibilityMode, menu.getVisibleItems().size());
    for (int i = 0; i < menu.size(); i++) {
      presenter.setUpdateSuspended(true);
      menu.getItem(i).setCheckable(true);
      presenter.setUpdateSuspended(false);
      BottomNavigationItemView child = getNewItem();
      buttons[i] = child;
      child.setIconTintList(itemIconTint);
      child.setIconSize(itemIconSize);
      
      child.setTextColor(itemTextColorDefault);
      child.setTextAppearanceInactive(itemTextAppearanceInactive);
      child.setTextAppearanceActive(itemTextAppearanceActive);
      child.setTextColor(itemTextColorFromUser);
      if (itemBackground != null) {
        child.setItemBackground(itemBackground);
      } else {
        child.setItemBackground(itemBackgroundRes);
      }
      child.setShifting(shifting);
      child.setLabelVisibilityMode(labelVisibilityMode);
      child.initialize((MenuItemImpl) menu.getItem(i), 0);
      child.setItemPosition(i);
      child.setOnClickListener(onClickListener);
      if (selectedItemId != Menu.NONE && menu.getItem(i).getItemId() == selectedItemId) {
        selectedItemPosition = i;
      }
      setBadgeIfNeeded(child);
      addView(child);
    }
    selectedItemPosition = Math.min(menu.size() - 1, selectedItemPosition);
    menu.getItem(selectedItemPosition).setChecked(true);
  }

  public void updateMenuView() {
    if (menu == null || buttons == null) {
      return;
    }

    final int menuSize = menu.size();
    if (menuSize != buttons.length) {
      
      buildMenuView();
      return;
    }

    int previousSelectedId = selectedItemId;

    for (int i = 0; i < menuSize; i++) {
      MenuItem item = menu.getItem(i);
      if (item.isChecked()) {
        selectedItemId = item.getItemId();
        selectedItemPosition = i;
      }
    }
    if (previousSelectedId != selectedItemId) {
      
      TransitionManager.beginDelayedTransition(this, set);
    }

    boolean shifting = isShifting(labelVisibilityMode, menu.getVisibleItems().size());
    for (int i = 0; i < menuSize; i++) {
      presenter.setUpdateSuspended(true);
      buttons[i].setLabelVisibilityMode(labelVisibilityMode);
      buttons[i].setShifting(shifting);
      buttons[i].initialize((MenuItemImpl) menu.getItem(i), 0);
      presenter.setUpdateSuspended(false);
    }
  }

  private BottomNavigationItemView getNewItem() {
    BottomNavigationItemView item = itemPool.acquire();
    if (item == null) {
      item = new BottomNavigationItemView(getContext());
    }
    return item;
  }

  public int getSelectedItemId() {
    return selectedItemId;
  }

  private boolean isShifting(@LabelVisibilityMode int labelVisibilityMode, int childCount) {
    return labelVisibilityMode == LabelVisibilityMode.LABEL_VISIBILITY_AUTO
        ? childCount > 3
        : labelVisibilityMode == LabelVisibilityMode.LABEL_VISIBILITY_SELECTED;
  }

  void tryRestoreSelectedItemId(int itemId) {
    final int size = menu.size();
    for (int i = 0; i < size; i++) {
      MenuItem item = menu.getItem(i);
      if (itemId == item.getItemId()) {
        selectedItemId = itemId;
        selectedItemPosition = i;
        item.setChecked(true);
        break;
      }
    }
  }

  SparseArray<BadgeDrawable> getBadgeDrawables() {
    return badgeDrawables;
  }

  void setBadgeDrawables(SparseArray<BadgeDrawable> badgeDrawables) {
    this.badgeDrawables = badgeDrawables;
    if (buttons != null) {
      for (BottomNavigationItemView itemView : buttons) {
        itemView.setBadge(badgeDrawables.get(itemView.getId()));
      }
    }
  }

  @Nullable
  BadgeDrawable getBadge(int menuItemId) {
    return badgeDrawables.get(menuItemId);
  }

  
  BadgeDrawable getOrCreateBadge(int menuItemId) {
    validateMenuItemId(menuItemId);
    BadgeDrawable badgeDrawable = badgeDrawables.get(menuItemId);
    
    if (badgeDrawable == null) {
      badgeDrawable = BadgeDrawable.create(getContext());
      badgeDrawables.put(menuItemId, badgeDrawable);
    }
    BottomNavigationItemView itemView = findItemView(menuItemId);
    if (itemView != null) {
      itemView.setBadge(badgeDrawable);
    }
    return badgeDrawable;
  }

  void removeBadge(int menuItemId) {
    validateMenuItemId(menuItemId);
    BadgeDrawable badgeDrawable = badgeDrawables.get(menuItemId);
    BottomNavigationItemView itemView = findItemView(menuItemId);
    if (itemView != null) {
      itemView.removeBadge();
    }
    if (badgeDrawable != null) {
      badgeDrawables.remove(menuItemId);
    }
  }

  private void setBadgeIfNeeded(@NonNull BottomNavigationItemView child) {
    int childId = child.getId();
    if (!isValidId(childId)) {
      
      return;
    }

    BadgeDrawable badgeDrawable = badgeDrawables.get(childId);
    if (badgeDrawable != null) {
      child.setBadge(badgeDrawable);
    }
  }

  private void removeUnusedBadges() {
    HashSet<Integer> activeKeys = new HashSet<>();
    
    for (int i = 0; i < menu.size(); i++) {
      activeKeys.add(menu.getItem(i).getItemId());
    }

    for (int i = 0; i < badgeDrawables.size(); i++) {
      int key = badgeDrawables.keyAt(i);
      if (!activeKeys.contains(key)) {
        badgeDrawables.delete(key);
      }
    }
  }

  @Nullable
  @VisibleForTesting
  BottomNavigationItemView findItemView(int menuItemId) {
    validateMenuItemId(menuItemId);
    if (buttons != null) {
      for (BottomNavigationItemView itemView : buttons) {
        if (itemView.getId() == menuItemId) {
          return itemView;
        }
      }
    }
    return null;
  }

  private boolean isValidId(int viewId) {
    return viewId != View.NO_ID;
  }

  private void validateMenuItemId(int viewId) {
    if (!isValidId(viewId)) {
      throw new IllegalArgumentException(viewId + " is not a valid view id");
    }
  }
}
