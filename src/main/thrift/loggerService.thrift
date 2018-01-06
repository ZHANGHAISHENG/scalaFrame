namespace java com.test.logger

exception WriteExce {
  string description
}

exception ReadException {
  string description
}

service LoggerService {
  string log(1: string message, 2: i32 logLevel) throws (1: WriteException writeEx);
  i32 getLogSize() throws (1: ReadException readEx);
}