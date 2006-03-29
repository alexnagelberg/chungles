JMODSUFFIX=@JMODSUFFIX@

INCLUDES = -I$(top_srcdir)
INCLUDES += @INCLTDL@
lib_LTLIBRARIES = libstatelessjniloader.la
libstatelessjniloader_la_SOURCES = statelessjniloader.c
libstatelessjniloader_la_LDFLAGS = -shrext $(JMODSUFFIX)
libstatelessjniloader_la_LIBADD = @LIBLTDL@
noinst_HEADERS = javalibraryloader.h
include_HEADERS = statelessapp.h
