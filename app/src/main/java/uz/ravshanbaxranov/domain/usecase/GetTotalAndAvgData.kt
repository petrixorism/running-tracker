package uz.ravshanbaxranov.domain.usecase

import uz.ravshanbaxranov.data.model.TotalData
import uz.ravshanbaxranov.domain.RunRepository



class GetTotalAndAvgData(
    private val repository: RunRepository
) {

    suspend operator fun invoke(): TotalData {
        return repository.getTotalAndAverageData()
    }


}