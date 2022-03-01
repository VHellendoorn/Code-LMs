import { DateTime } from "luxon";
import { BaseModel, column } from "@ioc:Adonis/Lucid/Orm";

export default class Post extends BaseModel {
  @column({ isPrimary: true })
  public id: number | string;

  @column()
  public userId: number | string;

  @column()
  public title: string;

  @column()
  public content: string;

  @column.dateTime({ autoCreate: true })
  public createdAt: DateTime;

  @column.dateTime({ autoCreate: true, autoUpdate: true })
  public updatedAt: DateTime;
}
