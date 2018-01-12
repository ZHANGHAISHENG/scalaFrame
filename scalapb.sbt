libraryDependencies += "com.trueaccord.scalapb" %% "scalapb-runtime" % com.trueaccord.scalapb.compiler.Version.scalapbVersion % "protobuf"

PB.protocVersion := "-v3.4.0"

PB.targets in Compile := Seq(
  scalapb.gen(
    flatPackage = true,
    javaConversions = false,
    grpc = false,
    singleLineToString = false
  ) -> (sourceManaged in Compile).value / "protobuf"
)
