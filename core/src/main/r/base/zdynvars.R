#  File src/library/base/R/zdynvars.R
#  Part of the R package, http://www.R-project.org
#
#  This program is free software; you can redistribute it and/or modify
#  it under the terms of the GNU General Public License as published by
#  the Free Software Foundation; either version 2 of the License, or
#  (at your option) any later version.
#
#  This program is distributed in the hope that it will be useful,
#  but WITHOUT ANY WARRANTY; without even the implied warranty of
#  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#  GNU General Public License for more details.
#
#  A copy of the GNU General Public License is available at
#  http://www.r-project.org/Licenses/

## Need to ensure this comes late enough ...
## Perhaps even merge it into the common profile?

.dynLibs <- local({
    ## <NOTE>
    ## Versions of R prior to 1.4.0 had .Dyn.libs in .AutoloadEnv
    ## (and did not always ensure getting it from there).
    ## Until 1.6.0, we consistently used the base environment.
    ## Now we have a dynamic variable instead.
    ## </NOTE>
    .Dyn.libs <- structure(list(), class = "DLLInfoList")
    function(new) {
        if(!missing(new)) {
            class(new) <- "DLLInfoList"
            .Dyn.libs <<- new
        }
        else
            .Dyn.libs
    }
})

.libPaths <- local({
    .lib.loc <- character(0L)            # Profiles need to set this.
    function(new) {
        if(!missing(new)) {
            new <- Sys.glob(path.expand(new))
            paths <- unique(path.expand(cnew, .Library.site, .Library)))
            .lib.loc <<- paths[file.info(paths)$isdir %in% TRUE]
        }
        else
            .lib.loc
    }
})
