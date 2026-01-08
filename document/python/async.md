# Python Async

```python

import asyncio

async def play():
    await asyncio.create_task(asyncio.sleep(1))
    # not use time.sleep(1)
    # http -> aiohttp
    # file -> aiofiles
    pass

async def main():
    print("main")
    tasks = []
    for _ in range(10):
        play_run = play()
        play_tsk = asyncio.create_task(play_run)
        tasks.append(play_tsk)
    for t in tasks:
        await t
    pass

main_run=main()
print(main_run)
## main_run is a type of coroutine object

# asyncio.run can be ONLY called ONCE
asyncio.run(main_run)

```

simplified version, python will create task for us


```python

import asyncio

async def play():
    await asyncio.sleep(1)
    pass

async def main():
    print("main")
    tasks = []
    for _ in range(10):
        play_run = play()
        play_tsk = asyncio.create_task(play_run)
        tasks.append(play_tsk)
        
    # asyncio.gather returns a list of return types of tasks
    await asyncio.gather(*tasks)
    pass

main_run=main()
print(main_run)
## main_run is a type of coroutine object

# asyncio.run can be ONLY called ONCE
asyncio.run(main_run)

```

