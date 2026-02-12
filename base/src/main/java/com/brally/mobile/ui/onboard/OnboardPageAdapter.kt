package com.brally.mobile.ui.onboard

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.brally.mobile.ui.onboard.content.ContentOnboardFragment
import com.language_onboard.data.model.OnboardingItem

class OnboardPageAdapter(fragment: Fragment) :
    FragmentStateAdapter(fragment) {

    private val items = mutableListOf<OnboardingItem>()

    fun setData(newList: List<OnboardingItem>) {
        items.clear()
        items.addAll(newList)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = items.size

    override fun createFragment(position: Int): Fragment {
        val isLast = (position == itemCount - 1)
        return ContentOnboardFragment.newInstance(items[position], isLast)
    }

    override fun getItemId(position: Int): Long = items[position].hashCode().toLong()

    override fun containsItem(itemId: Long): Boolean {
        return items.any { it.hashCode().toLong() == itemId }
    }
}
