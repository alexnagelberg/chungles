#ifndef _stateless_app_h_
#define _stateless_app_h_
#ifdef __cplusplus
extern "C"
{
#endif

typedef struct
{
	char *buf;
	long length;
	long required_mem;
} state;

char *get_app_id();
char *get_system_command();
state *freeze();
void thaw(state *st);
int has_enough_memory(long size);

#ifdef __cplusplus
}
#endif
#endif
