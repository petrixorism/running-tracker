package uz.ravshanbaxranov.domain.usecase

import uz.ravshanbaxranov.data.db.Run
import uz.ravshanbaxranov.domain.RunRepository

class AddRun(
    private val repository: RunRepository
) {

    suspend operator fun invoke(run: Run) {
        repository.addRun(run.toRunEntity())
    }

}