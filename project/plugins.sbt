addSbtPlugin("com.twitter"        % "scrooge-sbt-plugin"  % "17.10.0") // thrift
addSbtPlugin("org.scoverage"      % "sbt-scoverage"       % "1.5.1")   // 单元测试覆盖率
addSbtPlugin("com.thesamet"       % "sbt-protoc"          % "0.99.13") // scalaPB

libraryDependencies += "com.trueaccord.scalapb" %% "compilerplugin" % "0.6.6" // scalaPB