package uz.ravshanbaxranov.domain.usecase

data class RunUseCase(
    val addRun: AddRun,
    val deleteRun: DeleteRun,
    val getRuns: GetRuns,
    val getTotalAndAvgData: GetTotalAndAvgData
)