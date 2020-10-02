
cmake_minimum_required(VERSION 3.4.1)

add_library(

        cpp_code

        SHARED

        cpp_code.cpp
)

find_library( cpp_code

              log )

target_link_libraries(cpp_code

                      ${log-lib})

# Build the Oboe library #set (OBOE_DIR oboe/include/oboe)

#set (OBOE_DIR ./oboe) ######
#add_subdirectory (${OBOE_DIR} ./oboe
#       )
##add_subdirectory (${OBOE_DIR} "oboe/include/oboe")
#
## Specify the path to the Oboe header files and the source.
#include_directories(
#        ${OBOE_DIR}/include)
##        ${OBOE_DIR}/src
##        oboe/include/oboe
#
## Specify the libraries which our native library is dependent on, including Oboe
#target_link_libraries (cpp_code
#        log
#        oboe #${log-lib}
#        )

#find_package (oboe-1.4.3)
#target_link_libraries(app log oboe-1.4.3) # You may have other libraries here such as `log`.
