JMODSUFFIX=@JMODSUFFIX@

INCLUDES = -I$(top_srcdir)
lib_LTLIBRARIES = libstatelessjniloader.la
libstatelessjniloader_la_SOURCES = statelessjniloader.c
libstatelessjniloader_la_LDFLAGS = -shrext $(JMODSUFFIX)

noinst_HEADERS = javalibraryloader.h
include_HEADERS = statelessapp.h
