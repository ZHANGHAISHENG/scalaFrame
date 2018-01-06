namespace java in.xnnyygn.dictservice

exception DictServiceException {
  string description
}

service DictService {
  string get(1: string key) throws(1: DictServiceException ex)
  void put(1: string key, 2: string value)
}