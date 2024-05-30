ThisBuild / scmInfo := Some(
  ScmInfo(
    url("https://github.com/chryse-hdl/chryse"),
    "scm:git@github.com/chryse-hdl/chryse.git",
  ),
)

ThisBuild / developers := List(
  Developer(
    id = "kivikakk",
    name = "Asherah Connor",
    email = "ashe@kivikakk.ee",
    url = url("https://kivikakk.ee"),
  ),
)

ThisBuild / description := "Project framework for Chisel."
ThisBuild / licenses := List(
  "Apache 2" -> url("http://www.apache.org/licenses/LICENSE-2.0.txt"),
)

githubOwner       := "chryse-hdl"
githubRepository  := "chryse"
githubTokenSource := TokenSource.GitConfig("github.token")
