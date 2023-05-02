package uz.ravshanbaxranov.presentation.adapter

import android.annotation.SuppressLint
import android.icu.text.DecimalFormat
import android.icu.text.SimpleDateFormat
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import uz.ravshanbaxranov.data.db.Run
import uz.ravshanbaxranov.distancetracker.databinding.ItemRunBinding
import java.util.*


class HistoryAdapter : ListAdapter<Run, HistoryAdapter.Holder>(HistoryCallback) {

    private var onCLickListener: ((Run, ImageView) -> Unit)? = null


    object HistoryCallback : DiffUtil.ItemCallback<Run>() {
        override fun areItemsTheSame(oldItem: Run, newItem: Run): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Run, newItem: Run): Boolean =
            oldItem.id == newItem.id && oldItem.time == newItem.time
    }

    inner class Holder(private val binding: ItemRunBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SimpleDateFormat", "SetTextI18n")
        fun bind() {

            val item = getItem(absoluteAdapterPosition)
            val time = SimpleDateFormat("HH:mm dd-MMMM yyyy").format(Date(item.date))
            val duration = "${DecimalFormat("##.##").format(item.time / 60000.0f)} min"

            binding.runIv.transitionName = "trans_image$layoutPosition"

            binding.dateTv.text = time
            binding.runIv.setImageBitmap(item.image)
            binding.distanceTv.text =
                "${DecimalFormat("##.##").format(item.distanceInMeters / 1000.0)} km"

            val details = duration +
                    " | ${item.caloriesBurned} kcal" +
                    " | ${DecimalFormat("##.##").format(item.averageSpeed)} km/h"

            binding.detailsTv.text = details

        }

        init {
            binding.root.setOnClickListener {
                onCLickListener!!.invoke(getItem(absoluteAdapterPosition), binding.runIv as ImageView)
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder =
        Holder(ItemRunBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: Holder, position: Int) = holder.bind()


    fun setOnCLickListener(block: ((Run, ImageView) -> Unit)) {
        onCLickListener = block
    }


}
