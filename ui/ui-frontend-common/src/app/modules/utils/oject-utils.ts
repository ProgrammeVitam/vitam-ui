export class OjectUtils {
  public static valueNotUndefined(value: any) {
    return value !== undefined && value !== null;
  }

  public static arrayNotUndefined(value: any) {
    return value !== undefined && value !== null && value.length > 0;
  }
}
