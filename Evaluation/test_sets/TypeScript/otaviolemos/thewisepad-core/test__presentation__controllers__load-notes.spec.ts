import { MissingParamError } from '@/presentation/controllers/errors'
import { LoadNotesOperation } from '@/presentation/controllers/load-notes'
import { HttpRequest, HttpResponse } from '@/presentation/controllers/ports'
import { LoadNotes } from '@/use-cases/load-notes'
import { NoteData, NoteRepository } from '@/use-cases/ports'
import { NoteBuilder, UserBuilder } from '@test/builders'
import { InMemoryNoteRepository } from '@test/doubles/repositories'
import { ErrorThrowingUseCaseStub } from '@test/doubles/usecases'
import { WebController } from '@/presentation/controllers'

describe('Load notes controller', () => {
  test('should return 200 and notes for valid user', async () => {
    const note1: NoteData = NoteBuilder.aNote().build()
    const note2: NoteData = NoteBuilder.aNote().withDifferentTitleAndId().build()
    const noteRepositoryWithTwoNotes: NoteRepository = new InMemoryNoteRepository(
      [note1, note2]
    )
    const usecase: LoadNotes = new LoadNotes(noteRepositoryWithTwoNotes)
    const loadNotesController = new WebController(new LoadNotesOperation(usecase))
    const aUser = UserBuilder.aUser().build()
    const loadNotesRequest: HttpRequest = {
      body: {
        userId: aUser.id
      }
    }
    const response: HttpResponse = await loadNotesController.handle(loadNotesRequest)
    const loadResult = response.body as NoteData[]
    expect(loadResult.length).toEqual(2)
    expect(response.statusCode).toEqual(200)
  })

  test('should return 400 if request does not contain user id', async () => {
    const note1: NoteData = NoteBuilder.aNote().build()
    const noteRepositoryWithOneNote: NoteRepository = new InMemoryNoteRepository(
      [note1]
    )
    const usecase: LoadNotes = new LoadNotes(noteRepositoryWithOneNote)
    const loadNotesController = new WebController(new LoadNotesOperation(usecase))
    const requestWithoutUserId: HttpRequest = {
      body: {
      }
    }
    const response: HttpResponse = await loadNotesController.handle(requestWithoutUserId)
    const loadResult = response.body as Error
    expect(response.statusCode).toEqual(400)
    expect(loadResult).toBeInstanceOf(MissingParamError)
  })

  test('should return 500 if load notes use case throws', async () => {
    const errorThrowingLoadNotesUseCase = new ErrorThrowingUseCaseStub()
    const loadNotesController = new WebController(new LoadNotesOperation(errorThrowingLoadNotesUseCase))
    const aUser = UserBuilder.aUser().build()
    const loadNotesRequest: HttpRequest = {
      body: {
        userId: aUser.id
      }
    }
    const response: HttpResponse = await loadNotesController.handle(loadNotesRequest)
    expect(response.statusCode).toEqual(500)
  })
})
