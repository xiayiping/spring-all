
```python

from pydantic import BaseModel, Field, field_validator
from typing import Optional


class Person(BaseModel):
    name: str
    age: int = Field(frozen=True, lt=200)
    address: Optional[str] = None
    phones: list[str] = Field(default_factory=list)

    @field_validator("phones")
    @classmethod
    def validate_phones(cls, v: list[str]) -> list[str]:
        for n in v:
            if len(n) != 11:
                raise ValueError(f"Invalid phone {v}")
        return v


# 除了构造函数，
# 可以用python的字段关键字解包特性
info = {"name": "want", "age":18}
p = Person(**info)

# 也可以直接从 json 字符串构建
import json
info_json = json.dumps(info)
jp = Person.model_validate_json(info_json)

# convert back to json string
s = jp.model_dump_json()


```