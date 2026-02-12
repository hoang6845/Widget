package com.brally.mobile.ui.language

import androidx.core.content.ContextCompat
import com.brally.mobile.base.R
import com.brally.mobile.base.adapter.BaseRecyclerViewAdapter
import com.brally.mobile.base.databinding.ItemAppLanguageBinding
import com.brally.mobile.data.model.AppLanguageSelector

class AppLanguageAdapter : BaseRecyclerViewAdapter<AppLanguageSelector, ItemAppLanguageBinding>() {

    override fun bindData(
        binding: ItemAppLanguageBinding, item: AppLanguageSelector, position: Int
    ) {
        binding.apply {
            item.let {
                tvLanguage.text = it.language.countryName
                imvLanguage.setImageResource(item.language.imageRes)
                root.background = if (item.isCheck) {
                    ContextCompat.getDrawable(context, R.drawable.bg_lang_selected)
                } else {
                    ContextCompat.getDrawable(context, R.drawable.bg_lang_unselected)
                }
            }
        }

        binding.imvCheck.setImageResource(
            if (item.isCheck) R.drawable.ic_lang_checked
            else R.drawable.ic_lang_unchecked
        )
    }
}
