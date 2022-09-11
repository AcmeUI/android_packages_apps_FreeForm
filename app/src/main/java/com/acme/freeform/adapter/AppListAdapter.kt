/*
 * Copyright (C) 2022 Acme
 *
 * SPDX-License-Identifier: Apache-2.0
 *
 */

package com.acme.freeform.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.acme.freeform.R
import com.acme.freeform.model.App
import com.acme.freeform.view.FreeFormView

class AppListAdapter(private val context: Context) : RecyclerView.Adapter<AppListAdapter.Holder>() {
    class Holder(itemView: View) : RecyclerView.ViewHolder(itemView)

    var mAppList = mutableListOf<App>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(
            LayoutInflater.from(context).inflate(R.layout.item_app_list, parent, false)
        )
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val currentApp = mAppList[position]
        val imageViewAppIcon: ImageView =
            holder.itemView.rootView.findViewById(R.id.imageView_appIcon)
        val textViewAppName: TextView = holder.itemView.rootView.findViewById(R.id.textView_appName)
        imageViewAppIcon.setImageDrawable(currentApp.appIcon)
        imageViewAppIcon.setOnClickListener {
            val freeFormView = FreeFormView(context)
            freeFormView.addFreeForm(currentApp.packageName)
        }
        textViewAppName.text = currentApp.appName
    }

    override fun getItemCount(): Int {
        return mAppList.size
    }
}